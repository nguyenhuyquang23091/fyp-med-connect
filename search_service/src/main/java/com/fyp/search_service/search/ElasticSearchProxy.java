package com.fyp.search_service.search;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.fyp.search_service.constant.SuggestionType;
import com.fyp.search_service.dto.request.SearchFilter;
import com.fyp.search_service.dto.response.DoctorProfileResponse;
import com.fyp.search_service.dto.response.PageResponse;
import com.fyp.search_service.dto.response.SearchSuggestion;
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
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ElasticSearchProxy {

    ElasticsearchClient elasticsearchClient;
    DoctorProfileSearchMapper doctorProfileSearchMapper;

    public PageResponse<DoctorProfileResponse> searchDoctorByTerm(SearchFilter searchFilter){
        try {
            log.debug("Executing doctor search with filter: term={}, page={}, size={}",
                    searchFilter.getTerm(), searchFilter.getPage(), searchFilter.getSize());

            SearchResponse<DoctorProfile> searchResponse =
                    elasticsearchClient.search(QueryBuilder.buildDoctorSearch(searchFilter), DoctorProfile.class);

            long totalElements = searchResponse.hits().total().value();

            List<DoctorProfileResponse> doctorProfiles = searchResponse
                    .hits()
                    .hits().stream()
                    .map(Hit::source)
                    .map(doctorProfileSearchMapper::toDoctorProfileResponse)
                    .toList();

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
            throw handleElasticsearchException(e);
        } catch (Exception e) {
            log.error("Unexpected error during doctor search: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.ELASTICSEARCH_QUERY_ERROR);
        }
    }

    public List<SearchSuggestion> getSuggestions(String term, int limit) {
        try {
            log.debug("Executing suggestion query: term='{}', limit={}", term, limit);

            SearchResponse<DoctorProfile> searchResponse =
                    elasticsearchClient.search(QueryBuilder.buildSuggestiveSearch(term, limit), DoctorProfile.class);

            Set<String> seenTexts = new HashSet<>();

            List<SearchSuggestion> suggestions = searchResponse.hits().hits().stream()
                    .map(hit -> extractSuggestionsFromHit(hit.source(), hit.score(), term, seenTexts))
                    .flatMap(List::stream)
                    .sorted(Comparator.comparing(SearchSuggestion::getScore).reversed())
                    .limit(limit)
                    .toList();

            log.info("Suggestion query completed: term='{}', found {} unique suggestions", term, suggestions.size());

            return suggestions;

        } catch (IllegalArgumentException e) {
            log.warn("Invalid suggestion query parameters: {}", e.getMessage());
            throw new AppException(ErrorCode.INVALID_SEARCH_FILTER);
        } catch (IOException e) {
            log.error("ElasticSearch suggestion query failed for term '{}': {}", term, e.getMessage(), e);
            throw handleElasticsearchException(e);
        } catch (Exception e) {
            log.error("Unexpected error during suggestion query: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.ELASTICSEARCH_QUERY_ERROR);
        }
    }

    private List<SearchSuggestion> extractSuggestionsFromHit(DoctorProfile doctor, Double score,
                                                               String term, Set<String> seenTexts) {

        if (doctor == null) return List.of();

        float normalizedScore = normalizeScore(score);

        //revise this 2morrow
        String fullName = doctor.getLastName() + doctor.getFirstName();

        return Stream.of(
                createSuggestion(fullName, SuggestionType.DOCTOR_NAME, normalizedScore,
                        doctor.getDoctorProfileId(), doctor.getResidency(), seenTexts),

                createSuggestion(doctor.getResidency(), SuggestionType.RESIDENCY,
                        normalizedScore * 0.8f, null, "Location", seenTexts),

                extractNestedSuggestions(doctor.getSpecialties(), term,
                        DoctorProfile.SpecialtyInfo::getSpecialtyName,
                        specialty -> specialty.getSpecialtyId() != null ? specialty.getSpecialtyId().toString() : null,
                        DoctorProfile.SpecialtyInfo::getSpecialtyDescription,
                        SuggestionType.SPECIALTY, normalizedScore * 0.9f, seenTexts),

                extractNestedSuggestions(doctor.getServices(), term,
                        DoctorProfile.ServiceInfo::getServiceName,
                        service -> service.getServiceId() != null ? service.getServiceId().toString() : null,
                        service -> service.getPrice() != null ? service.getPrice() + " " + service.getCurrency() : null,
                        SuggestionType.SERVICE, normalizedScore * 0.85f, seenTexts),

                extractNestedSuggestions(doctor.getExperiences(), term,
                        DoctorProfile.ExperienceInfo::getHospitalName,
                        exp -> null,
                        DoctorProfile.ExperienceInfo::getLocation,
                        SuggestionType.HOSPITAL, normalizedScore * 0.75f, seenTexts)
        )
        .flatMap(List::stream)
        .toList();
    }

    private List<SearchSuggestion> createSuggestion(String text, SuggestionType type, Float score,
                                                      String entityId, String description, Set<String> seenTexts) {
        if (text == null || text.isBlank()) return List.of();

        if (!seenTexts.add(text.toLowerCase())) return List.of();

        return List.of(SearchSuggestion.builder()
                .text(text)
                .type(type)
                .score(score)
                .entityId(entityId)
                .description(description)
                .build());
    }

    private <T> List<SearchSuggestion> extractNestedSuggestions(List<T> nestedList, String term,
                                                                  Function<T, String> textExtractor,
                                                                  Function<T, String> entityIdExtractor,
                                                                  Function<T, String> descriptionExtractor,
                                                                  SuggestionType type, Float score,
                                                                  Set<String> seenTexts) {
        if (nestedList == null) return List.of();

        return nestedList.stream()
                .map(item -> {
                    String text = textExtractor.apply(item);
                    if (text == null || !text.toLowerCase().contains(term.toLowerCase())) return null;
                    if (!seenTexts.add(text.toLowerCase())) return null;

                    return SearchSuggestion.builder()
                            .text(text)
                            .type(type)
                            .score(score)
                            .entityId(entityIdExtractor.apply(item))
                            .description(descriptionExtractor.apply(item))
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private float normalizeScore(Double score) {
        if (score == null) return 0.0f;
        return Math.min(score.floatValue() / 10.0f, 1.0f);
    }

    private AppException handleElasticsearchException(IOException e) {
        if (e.getMessage() != null && e.getMessage().contains("Connection refused")) {
            return new AppException(ErrorCode.ELASTICSEARCH_CONNECTION_ERROR);
        }
        return new AppException(ErrorCode.ELASTICSEARCH_QUERY_ERROR);
    }
}
