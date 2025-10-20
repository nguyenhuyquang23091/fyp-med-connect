package com.fyp.search_service.repository;

import com.fyp.search_service.entity.AppointmentEntity;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface AppointmentSearchRepository extends ElasticsearchRepository<AppointmentEntity, String> {

}
