package com.fyp.profile_service.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import com.fyp.profile_service.entity.UserProfile;

@Repository
public interface UserProfileRepository extends Neo4jRepository<UserProfile, String> {
    Optional<UserProfile> findByUserId(String userId);

    List<UserProfile> findAllByUserIdIn(Set<String> userIds);

    Page<UserProfile> findAllByUserIdIn(Set<String> userIds, Pageable pageable);


    Page<UserProfile> findAllBy(Pageable pageable);
}
