package com.fyp.profile_service.entity;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

import lombok.*;
import lombok.experimental.FieldDefaults;

@RelationshipProperties
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DoctorExperienceRelationship {

    @Id
    @GeneratedValue
    Long id;

    @TargetNode
    PracticeExperience experience;

    @Property("displayOrder")
    Integer displayOrder;

    @Property("isHighlighted")
    Boolean isHighlighted;
}
