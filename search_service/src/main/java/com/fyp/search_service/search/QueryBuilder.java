package com.fyp.search_service.search;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import com.fyp.search_service.dto.request.SearchFilter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class QueryBuilder {

    public static SearchRequest buildSearchRequest(SearchFilter searchFilter){
        SearchRequest.Builder builder = new SearchRequest.Builder();
        builder.index("doctor_profiles");
        MatchQuery.Builder matchQuery = new MatchQuery.Builder();
        matchQuery.field("residency");
        matchQuery.query(searchFilter.getTerm());
        Query.Builder queryBuilder = new Query.Builder();

        queryBuilder.match(matchQuery.build());
        builder.query(queryBuilder.build());

        return builder.build();

    }
}
