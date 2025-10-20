package com.fyp.search_service.repository;

import com.fyp.search_service.entity.UserEntity;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UserSearchRepository extends ElasticsearchRepository<UserEntity, String> {



}
