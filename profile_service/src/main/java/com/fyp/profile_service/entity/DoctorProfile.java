package com.fyp.profile_service.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Node("doctor_profile")
public class DoctorProfile {

    @Id
    @GeneratedValue(generatorClass = UUIDStringGenerator.class)
    String id;

    @Property("userId")
    String userId;

    @Property("residency")
    String residency;

    @Property("yearsOfExperience")
    Integer yearsOfExperience;

    @Property("bio")
    String bio;

    @Property("isAvailable")
    Boolean isAvailable;

    @Property("languages")
    List<String> languages;

    // ==================== Audit Fields ====================

    @Property("createdAt")
    LocalDateTime createdAt;

    LocalDateTime updatedAt;

    // ==================== Relationships ====================

    @Relationship(type = "HAS_SPECIALTY", direction = Relationship.Direction.OUTGOING)
    Set<DoctorSpecialtyRelationship> specialties = new HashSet<>();

    @Relationship(type = "HAS_EXPERIENCE", direction = Relationship.Direction.OUTGOING)
    Set<DoctorExperienceRelationship> practiceExperience = new HashSet<>();

    @Relationship(type = "OFFERS_SERVICE", direction = Relationship.Direction.OUTGOING)
    Set<DoctorServiceRelationship> services = new HashSet<>();
}
