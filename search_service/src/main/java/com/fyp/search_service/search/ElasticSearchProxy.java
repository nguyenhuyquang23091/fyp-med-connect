package com.fyp.search_service.search;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.fyp.search_service.dto.request.SearchFilter;
import com.fyp.search_service.dto.response.DoctorProfileResponse;
import com.fyp.search_service.dto.response.PageResponse;
import com.fyp.search_service.entity.DoctorProfile;
import com.fyp.search_service.exceptions.AppException;
import com.fyp.search_service.exceptions.ErrorCode;
import com.fyp.search_service.mapper.DoctorProfileSearchMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ElasticSearchProxy {

    ElasticsearchClient elasticsearchClient;
    DoctorProfileSearchMapper doctorProfileSearchMapper;

    //Later will be converted to use GEneric Type for multiple search ( appointments, users)

    public PageResponse<DoctorProfileResponse> searchDoctorByTerm(SearchFilter searchFilter){
        try {
            log.debug("Executing doctor search with filter: term={}, page={}, size={}",
                    searchFilter.getTerm(), searchFilter.getPage(), searchFilter.getSize());

            SearchResponse<DoctorProfile> searchResponse =
                    elasticsearchClient.search(QueryBuilder.buildDoctorSearch(searchFilter), DoctorProfile.class);

            // Reason: Extract total count from Elasticsearch for pagination metadata
            long totalElements = searchResponse.hits().total().value();

            // Reason: Extract doctor profiles from Elasticsearch hits and map to response DTOs
            List<DoctorProfileResponse> doctorProfiles = searchResponse
                    .hits()
                    .hits().stream()
                    .map(hit -> hit.source())
                    .map(doctorProfileSearchMapper::toDoctorProfileResponse)
                    .toList();

            // Reason: Calculate total pages based on page size
            int totalPages = (int) Math.ceil((double) totalElements / searchFilter.getSize());

            log.info("Search completed successfully. Found {} total results, returning page {} of {}",
                    totalElements, searchFilter.getPage() + 1, totalPages);

            return PageResponse.<DoctorProfileResponse>builder()
                    .currentPage(searchFilter.getPage())
                    .pageSize(searchFilter.getSize())
                    .totalPages(totalPages)
                    .totalElements(totalElements)
                    .data(doctorProfiles)
                    .build();

        } catch (IOException e) {
            log.error("Elasticsearch query failed for search term '{}': {}",
                    searchFilter.getTerm(), e.getMessage(), e);

            if (e.getMessage() != null && e.getMessage().contains("Connection refused")) {
                throw new AppException(ErrorCode.ELASTICSEARCH_CONNECTION_ERROR);
            }

            throw new AppException(ErrorCode.ELASTICSEARCH_QUERY_ERROR);
        } catch (Exception e) {
            // Reason: Catch any unexpected exceptions during query building or mapping
            log.error("Unexpected error during doctor search: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.ELASTICSEARCH_QUERY_ERROR);
        }
    }


}
