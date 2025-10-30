package com.fyp.search_service.search;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.fyp.search_service.dto.request.SearchFilter;
import com.fyp.search_service.dto.response.DoctorProfileResponse;
import com.fyp.search_service.entity.DoctorProfile;
import com.fyp.search_service.mapper.DoctorProfileSearchMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ElasticSearchProxy {
    ElasticsearchClient elasticsearchClient;
    DoctorProfileSearchMapper doctorProfileSearchMapper;

    public List<DoctorProfileResponse> searchDoctor(SearchFilter searchFilter){
        try {
            SearchResponse<DoctorProfile> doctorProfileSearchResponse =
                    elasticsearchClient.search(QueryBuilder.buildSearchRequest(searchFilter), DoctorProfile.class);
            //list<E> represent for what class we have just found update later
            List<DoctorProfile> doctorProfilesList = doctorProfileSearchResponse
                    .hits()
                    .hits().stream()
                    .map(hit -> hit.source())
                    .toList();
           return doctorProfilesList.stream().map(doctorProfileSearchMapper::toDoctorProfileResponse).toList();

        } catch (IOException e) {
            log.error("Error executing doctor search: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
