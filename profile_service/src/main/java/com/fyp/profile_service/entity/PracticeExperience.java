package com.fyp.profile_service.entity;

import java.time.LocalDate;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Node("practice_experience")
public class PracticeExperience {
    @Id
    @GeneratedValue(generatorClass = UUIDStringGenerator.class)
    String id;

    @Property("hospitalName")
    String hospitalName;

    @Property("hospitalLogo")
    String hospitalLogo;

    @Property("department")
    String department;

    @Property("location")
    String location;

    @Property("country")
    String country;

    @Property("position")
    String position;

    @Property("startDate")
    LocalDate startDate;

    @Property("endDate")
    LocalDate endDate;

    @Property("isCurrent")
    Boolean isCurrent;

    @Property("description")
    String description;

    @Property("displayOrder")
    Integer displayOrder;
}
