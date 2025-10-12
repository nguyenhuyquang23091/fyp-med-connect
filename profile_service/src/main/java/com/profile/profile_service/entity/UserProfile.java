package com.profile.profile_service.entity;

import java.time.LocalDate;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * UserProfile entity representing user profile information in Neo4j.
 *
 * Note: Role information is NOT stored here to maintain single source of truth.
 * Roles are managed by AuthService and propagated via JWT tokens.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Node("user_profile")
public class UserProfile {
    @Id
    @GeneratedValue(generatorClass = UUIDStringGenerator.class)
    String id;

    // userId from authservice (single source of truth for user identity)
    @Property("userId")
    String userId;

    @Property("email")
    String email;

    String avatar;
    String firstName;
    String lastName;
    LocalDate dob;
    String city;
}
