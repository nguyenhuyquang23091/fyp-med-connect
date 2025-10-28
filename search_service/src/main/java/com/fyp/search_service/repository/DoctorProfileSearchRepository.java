package com.fyp.search_service.repository;

import com.fyp.search_service.entity.DoctorProfile;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface DoctorProfileSearchRepository extends ElasticsearchRepository<DoctorProfile, String> {

}
