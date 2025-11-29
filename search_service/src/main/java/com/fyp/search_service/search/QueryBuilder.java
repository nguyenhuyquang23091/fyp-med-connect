package com.fyp.search_service.search;


import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import com.fyp.search_service.dto.request.AppointmentSearchFilter;
import com.fyp.search_service.dto.request.SearchFilter;
import com.fyp.search_service.dto.request.UserSearchFilter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
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

        addFuzzyTextSearch(boolQuery , searchFilter.getTerm());
        addBooleanFilters(boolQuery, searchFilter);
        addRangeFilter(boolQuery, searchFilter);
        return buildSearchRequest(boolQuery, searchFilter);
    }

    private static SearchRequest buildSearchRequest(BoolQuery.Builder boolQuery, SearchFilter searchFilter) {
        SearchRequest.Builder builder = new SearchRequest.Builder();
        builder.index("doctor_profiles");


        Query query = Query.of(q -> q.bool(boolQuery.build()));
        builder.query(query);

        // Pagination
        int from = (searchFilter.getPage() - 1) * searchFilter.getSize();
        builder.from(from);
        builder.size(searchFilter.getSize());

        // Sorting
        List<SortOptions> sortOptions = buildSortOptions(searchFilter.getSortBy(), searchFilter.getSortOrder());
        builder.sort(sortOptions);

        log.debug("Built search request: index=doctor_profiles, from={}, size={}", from, searchFilter.getSize());

        return builder.build();
    }

    private static void addFuzzyTextSearch(BoolQuery.Builder boolQuery, String term) {
        if (term== null || term.isBlank()) {
            return;
        }

        // Top-level field: residency (boost: 1.5)
        boolQuery.should(s -> s
                .match(m -> m
                        .field("residency")
                        .query(term)
                        .fuzziness("AUTO")
                        .prefixLength(2)
                        .boost(1.5f)
                )
        );

        
        // Reason: Search firstName with highest priority (boost: 2.5) for better name matching
        boolQuery.should(s -> s
                .match(m -> m
                        .field("firstName")
                        .query(term)
                        .fuzziness("AUTO")
                        .prefixLength(1)
                        .boost(2.5f)
                )
        );

        // Reason: Search lastName with high priority (boost: 2.0) for better name matching
        boolQuery.should(s -> s
                .match(m -> m
                        .field("lastName")
                        .query(term)
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
                                        .query(term)
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
                                        .query(term)
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
                                        .query(term)
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

    public static SearchRequest buildSuggestiveSearch(String term, int limit){
        if(term == null){
            throw new IllegalArgumentException("Search term must be at least 2 characters");
        }

        BoolQuery.Builder boolQuery = new BoolQuery.Builder();
        addFuzzyTextSearch(boolQuery, term);

        SearchRequest.Builder builder = new SearchRequest.Builder();
        builder.index("doctor_profiles");
        builder.query(Query.of(q -> q.bool(boolQuery.build())));
        builder.size(Math.min(limit, 10));
        builder.source(src -> src.filter(f -> f
                        .includes("doctorProfileId", "firstName", "lastName", "residency", "avatar",
                                "specialties.specialtyName", "services.serviceName",
                                "experiences.hospitalName")));
        builder.sort(SortOptions.of(s -> s.score(sc -> sc.order(SortOrder.Desc))));

        return  builder.build();


    }


    //Appointment Search
    public static SearchRequest buildAppointmentSearch(AppointmentSearchFilter filter) {
        BoolQuery.Builder boolQuery = new BoolQuery.Builder();

        addAppointmentTextSearch(boolQuery, filter.getTerm());
        addAppointmentFilters(boolQuery, filter);
        addAppointmentDateRangeFilter(boolQuery, filter);

        return buildAppointmentSearchRequest(boolQuery, filter);
    }

    private static void addAppointmentTextSearch(BoolQuery.Builder boolQuery, String term) {
        if (term == null || term.isBlank()) {
            return;
        }

        // Search patient name (boost: 2.0)
        boolQuery.should(s -> s
                .match(m -> m
                        .field("patientFullName")
                        .query(term)
                        .fuzziness("AUTO")
                        .prefixLength(1)
                        .boost(2.0f)
                )
        );

        // Search doctor name (boost: 2.0)
        boolQuery.should(s -> s
                .match(m -> m
                        .field("doctorFullName")
                        .query(term)
                        .fuzziness("AUTO")
                        .prefixLength(1)
                        .boost(2.0f)
                )
        );

        // Search specialty (boost: 1.5)
        boolQuery.should(s -> s
                .match(m -> m
                        .field("specialty")
                        .query(term)
                        .fuzziness("AUTO")
                        .prefixLength(2)
                        .boost(1.5f)
                )
        );

        // Search services (boost: 1.2)
        boolQuery.should(s -> s
                .match(m -> m
                        .field("services")
                        .query(term)
                        .fuzziness("AUTO")
                        .prefixLength(2)
                        .boost(1.2f)
                )
        );

        // Search reasons (boost: 1.0)
        boolQuery.should(s -> s
                .match(m -> m
                        .field("reasons")
                        .query(term)
                        .fuzziness("AUTO")
                        .boost(1.0f)
                )
        );
    }

    private static void addAppointmentFilters(BoolQuery.Builder boolQuery, AppointmentSearchFilter filter) {
        // Filter by userId
        if (filter.getUserId() != null && !filter.getUserId().isBlank()) {
            boolQuery.filter(f -> f
                    .term(t -> t
                            .field("userId")
                            .value(filter.getUserId())
                    )
            );
        }

        // Filter by doctorId
        if (filter.getDoctorId() != null && !filter.getDoctorId().isBlank()) {
            boolQuery.filter(f -> f
                    .term(t -> t
                            .field("doctorId")
                            .value(filter.getDoctorId())
                    )
            );
        }

        // Filter by appointment status
        if (filter.getAppointmentStatus() != null && !filter.getAppointmentStatus().isBlank()) {
            boolQuery.filter(f -> f
                    .term(t -> t
                            .field("appointmentStatus")
                            .value(filter.getAppointmentStatus())
                    )
            );
        }

        // Filter by consultation type
        if (filter.getConsultationType() != null && !filter.getConsultationType().isBlank()) {
            boolQuery.filter(f -> f
                    .term(t -> t
                            .field("consultationType")
                            .value(filter.getConsultationType())
                    )
            );
        }

        // Filter by payment method
        if (filter.getPaymentMethod() != null && !filter.getPaymentMethod().isBlank()) {
            boolQuery.filter(f -> f
                    .term(t -> t
                            .field("paymentMethod")
                            .value(filter.getPaymentMethod())
                    )
            );
        }

        // Filter by specialty
        if (filter.getSpecialty() != null && !filter.getSpecialty().isBlank()) {
            boolQuery.filter(f -> f
                    .term(t -> t
                            .field("specialty")
                            .value(filter.getSpecialty())
                    )
            );
        }
    }

    private static void addAppointmentDateRangeFilter(BoolQuery.Builder boolQuery, AppointmentSearchFilter filter) {
        // Range filter for appointment date
        if (filter.getStartDate() != null || filter.getEndDate() != null) {
            boolQuery.filter(f -> f.range(rq -> rq.date(drq -> {
                drq.field("appointmentDateTime");

                if (filter.getStartDate() != null) {
                    drq.gte(filter.getStartDate().atZone(ZoneId.systemDefault()).toInstant().toString());
                }

                if (filter.getEndDate() != null) {
                    drq.lte(filter.getEndDate().atZone(ZoneId.systemDefault()).toInstant().toString());
                }

                return drq;
            })));
        }
    }

    private static SearchRequest buildAppointmentSearchRequest(BoolQuery.Builder boolQuery, AppointmentSearchFilter filter) {
        SearchRequest.Builder builder = new SearchRequest.Builder();
        builder.index("appointment");

        Query query = Query.of(q -> q.bool(boolQuery.build()));
        builder.query(query);

        // Pagination
        int from = (filter.getPage() - 1) * filter.getSize();
        builder.from(from);
        builder.size(filter.getSize());

        // Sorting
        List<SortOptions> sortOptions = buildSortOptions(filter.getSortBy(), filter.getSortOrder());
        builder.sort(sortOptions);

        log.debug("Built appointment search request: index=appointment, from={}, size={}", from, filter.getSize());

        return builder.build();
    }


    public static SearchRequest buildAppointmentSuggestiveSearch(String term, int limit, String userId, String doctorId) {
        if (term == null || term.isBlank()) {
            throw new IllegalArgumentException("Search term must be at least 2 characters");
        }

        BoolQuery.Builder boolQuery = new BoolQuery.Builder();
        addAppointmentTextSearch(boolQuery, term);

        if (userId != null && !userId.isBlank()) {
            boolQuery.filter(f -> f.term(t -> t.field("userId").value(userId)));
        }

        if (doctorId != null && !doctorId.isBlank()) {
            boolQuery.filter(f -> f.term(t -> t.field("doctorId").value(doctorId)));
        }

        SearchRequest.Builder builder = new SearchRequest.Builder();
        builder.index("appointment");
        builder.query(Query.of(q -> q.bool(boolQuery.build())));
        builder.size(Math.min(limit, 10));
        builder.source(src -> src.filter(f -> f
                .includes("id", "patientFullName", "doctorFullName", "specialty",
                         "appointmentDateTime", "appointmentStatus")));
        builder.sort(SortOptions.of(s -> s.score(sc -> sc.order(SortOrder.Desc))));

        return builder.build();
    }

    // ==================== USER SEARCH ====================

    public static SearchRequest buildUserSearch(UserSearchFilter filter) {
        BoolQuery.Builder boolQuery = new BoolQuery.Builder();

        addUserTextSearch(boolQuery, filter.getTerm());
        addUserFilters(boolQuery, filter);

        return buildUserSearchRequest(boolQuery, filter);
    }

    private static void addUserTextSearch(BoolQuery.Builder boolQuery, String term) {
        if (term == null || term.isBlank()) {
            return;
        }

        // Search username (boost: 2.0)
        boolQuery.should(s -> s
                .match(m -> m
                        .field("username")
                        .query(term)
                        .fuzziness("AUTO")
                        .prefixLength(1)
                        .boost(2.0f)
                )
        );

        // Search email (boost: 1.5)
        boolQuery.should(s -> s
                .match(m -> m
                        .field("email")
                        .query(term)
                        .boost(1.5f)
                )
        );
    }

    private static void addUserFilters(BoolQuery.Builder boolQuery, UserSearchFilter filter) {
        // Filter by role
        if (filter.getRole() != null && !filter.getRole().isBlank()) {
            boolQuery.filter(f -> f
                    .term(t -> t
                            .field("role")
                            .value(filter.getRole())
                    )
            );
        }
    }

    private static SearchRequest buildUserSearchRequest(BoolQuery.Builder boolQuery, UserSearchFilter filter) {
        SearchRequest.Builder builder = new SearchRequest.Builder();
        builder.index("users");

        Query query = Query.of(q -> q.bool(boolQuery.build()));
        builder.query(query);

        // Pagination
        int from = (filter.getPage() - 1) * filter.getSize();
        builder.from(from);
        builder.size(filter.getSize());

        // Sorting
        List<SortOptions> sortOptions = buildSortOptions(filter.getSortBy(), filter.getSortOrder());
        builder.sort(sortOptions);

        log.debug("Built user search request: index=users, from={}, size={}", from, filter.getSize());

        return builder.build();
    }


    public static SearchRequest buildUserSuggestiveSearch(String term, int limit) {
        if (term == null || term.isBlank()) {
            throw new IllegalArgumentException("Search term must be at least 2 characters");
        }

        BoolQuery.Builder boolQuery = new BoolQuery.Builder();
        addUserTextSearch(boolQuery, term);

        SearchRequest.Builder builder = new SearchRequest.Builder();
        builder.index("users");
        builder.query(Query.of(q -> q.bool(boolQuery.build())));
        builder.size(Math.min(limit, 10));
        builder.source(src -> src.filter(f -> f
                .includes("id", "username", "email", "role")));
        builder.sort(SortOptions.of(s -> s.score(sc -> sc.order(SortOrder.Desc))));

        return builder.build();
    }

    // ==================== HELPER METHODS ====================

    private static List<SortOptions> buildSortOptions(String sortField, SortOrder sortOrder) {
        List<SortOptions> sortOptions = new ArrayList<>();

        if (sortField != null && !sortField.isBlank()) {
            SortOrder order = sortOrder != null ? sortOrder : SortOrder.Asc;
            sortOptions.add(SortOptions.of(s -> s.field(f -> f
                    .field(sortField)
                    .order(order)
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
