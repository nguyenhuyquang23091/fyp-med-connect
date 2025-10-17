package com.fyp.profile_service.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import com.fyp.profile_service.entity.PracticeExperience;

@Repository
public interface PracticeExperienceRepository extends Neo4jRepository<PracticeExperience, String> {}
