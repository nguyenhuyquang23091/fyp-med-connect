package com.fyp.profile_service.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import com.fyp.profile_service.entity.MedicalService;

@Repository
public interface MedicalServiceRepository extends Neo4jRepository<MedicalService, String> {

    boolean existsByName(String name);
}
