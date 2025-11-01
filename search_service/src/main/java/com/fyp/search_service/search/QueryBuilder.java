package com.fyp.search_service.search;


import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import com.fyp.search_service.dto.request.SearchFilter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class QueryBuilder {

    public static SearchRequest buildDoctorSearch(SearchFilter searchFilter) {
        BoolQuery.Builder boolQuery = new BoolQuery.Builder();

        addAutoCompleteQueries(boolQuery, searchFilter);
        addBooleanFilters(boolQuery, searchFilter);
        addRangeFilter(boolQuery, searchFilter);

        return buildSearchRequest(boolQuery, searchFilter);
    }

    private static SearchRequest buildSearchRequest(BoolQuery.Builder boolQuery, SearchFilter searchFilter) {
        SearchRequest.Builder builder = new SearchRequest.Builder();
        builder.index("doctor_profiles");

        // Build and set the query
        //Build Fuzzy queries

        Query query = Query.of(q -> q.bool(boolQuery.build()));
        builder.query(query);

        // Pagination
        int from = searchFilter.getPage() * searchFilter.getSize();
        builder.from(from);
        builder.size(searchFilter.getSize());

        // Sorting
        List<SortOptions> sortOptions = sortOptions(searchFilter);
        builder.sort(sortOptions);

        log.debug("Built search request: index=doctor_profiles, from={}, size={}", from, searchFilter.getSize());

        return builder.build();
    }

    private static void addAutoCompleteQueries(BoolQuery.Builder boolQuery, SearchFilter searchFilter) {
        if (searchFilter.getTerm() == null || searchFilter.getTerm().isBlank()) {
            return;
        }

        // Top-level field: residency (boost: 1.5)
        boolQuery.should(s -> s
                .match(m -> m
                        .field("residency")
                        .query(searchFilter.getTerm())
                        .fuzziness("AUTO")
                        .prefixLength(2)
                        .boost(1.5f)
                )
        );
        //full Name - highest priority ( 2.0)
        boolQuery.should(s -> s
                .match(m -> m
                        .field("fullName")
                        .query(searchFilter.getTerm())
                        .fuzziness("AUTO")
                        .prefixLength(1)
                        .boost(2.0f)
                )
        );

        // Nested field: specialtyName
        boolQuery.should(s -> s
                .nested(n -> n
                        .path("specialties")
                        .query(q -> q
                                .match(m -> m
                                        .field("specialties.specialtyName")
                                        .query(searchFilter.getTerm())
                                        .fuzziness("AUTO")
                                        .prefixLength(2)
                                        .boost(1.5f)
                                )
                        )
                        .scoreMode(ChildScoreMode.Avg)
                )
        );

        // Nested field: hospitalName (boost: 1.3)
        boolQuery.should(s -> s
                .nested(n -> n
                        .path("experiences")
                        .query(q -> q
                                .match(m -> m
                                        .field("experiences.hospitalName")
                                        .query(searchFilter.getTerm())
                                        .fuzziness("AUTO")
                                        .prefixLength(2)
                                        .boost(1.3f)
                                )
                        )
                        .scoreMode(ChildScoreMode.Max)
                )
        );

        // Nested field: serviceName (boost: 1.2)
        boolQuery.should(s -> s
                .nested(n -> n
                        .path("services")
                        .query(q -> q
                                .match(m -> m
                                        .field("services.serviceName")
                                        .query(searchFilter.getTerm())
                                        .fuzziness("AUTO")
                                        .prefixLength(2)
                                        .boost(1.2f)
                                )
                        )
                        .scoreMode(ChildScoreMode.Max)
                )
        );


    }

    private static void addBooleanFilters(BoolQuery.Builder boolQuery, SearchFilter searchFilter) {
        // Search by availability
        if (searchFilter.getIsAvailable() != null) {
            boolQuery.filter(f -> f
                    .term(t -> t
                            .field("isAvailable")
                            .value(searchFilter.getIsAvailable())
                    )
            );
        }

        // Search by language
        if (searchFilter.getLanguages() != null && !searchFilter.getLanguages().isEmpty()) {
            List<FieldValue> languageValues = searchFilter.getLanguages()
                    .stream()
                    .map(FieldValue::of)
                    .collect(Collectors.toList());

            boolQuery.filter(f -> f
                    .terms(t -> t
                            .field("languages")
                            .terms(tv -> tv.value(languageValues))
                    )
            );
        }
    }

    private static void addRangeFilter(BoolQuery.Builder boolQuery, SearchFilter searchFilter) {
        // Range filter for years of experience
        if (searchFilter.getMinYearsOfExperience() != null || searchFilter.getMaxYearsOfExperience() != null) {
            boolQuery.filter(f -> f.range(rq -> rq.number(nrq -> {
                nrq.field("yearsOfExperience");

                if (searchFilter.getMinYearsOfExperience() != null) {
                    nrq.gte(searchFilter.getMinYearsOfExperience().doubleValue());
                }

                if (searchFilter.getMaxYearsOfExperience() != null) {
                    nrq.lte(searchFilter.getMaxYearsOfExperience().doubleValue());
                }

                return nrq;
            })));
        }

        // Range filter for price (nested in services)
        if (searchFilter.getMinPrice() != null || searchFilter.getMaxPrice() != null) {
            boolQuery.filter(f -> f
                    .nested(nq -> nq
                            .path("services")
                            .query(q -> q.range(rq -> rq.number(nrq -> {
                                nrq.field("services.price");

                                if (searchFilter.getMinPrice() != null) {
                                    nrq.gte(searchFilter.getMinPrice().doubleValue());
                                }

                                if (searchFilter.getMaxPrice() != null) {
                                    nrq.lte(searchFilter.getMaxPrice().doubleValue());
                                }

                                return nrq;
                            })))
                    )
            );
        }
    }

    private static List<SortOptions> sortOptions(SearchFilter searchFilter) {
        List<SortOptions> sortOptions = new ArrayList<>();
        String sortedField = searchFilter.getSortBy();

        if (sortedField != null && !sortedField.isBlank()) {
            SortOrder sortOrder = searchFilter.getSortOrder() != null ? searchFilter.getSortOrder() : SortOrder.Asc;
            sortOptions.add(SortOptions.of(s -> s.field(f -> f
                    .field(sortedField)
                    .order(sortOrder)
            )));
        } else {
            sortOptions.add(SortOptions.of(s -> s
                    .score(sc -> sc.order(SortOrder.Desc))
            ));
            log.debug("Using default sorting by relevance (_score)");
        }

        return sortOptions;
    }
}
