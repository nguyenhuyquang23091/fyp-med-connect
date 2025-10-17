package com.fyp.profile_service.repository;

import java.util.Optional;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import com.fyp.profile_service.entity.Specialty;

@Repository
public interface SpecialtyRepository extends Neo4jRepository<Specialty, String> {

    Optional<Specialty> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByName(String name);
}
