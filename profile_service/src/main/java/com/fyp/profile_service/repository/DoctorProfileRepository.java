package com.fyp.profile_service.repository;

import java.util.Optional;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import com.fyp.profile_service.entity.DoctorProfile;

@Repository
public interface DoctorProfileRepository extends Neo4jRepository<DoctorProfile, String> {

    Optional<DoctorProfile> findByUserId(String userId);

    boolean existsByUserId(String userId);
}
