package com.fyp.profile_service.entity;

import java.time.LocalDate;

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
public class DoctorSpecialtyRelationship {

    @Id
    @GeneratedValue
    Long id;

    @TargetNode
    Specialty specialty;

    @Property("isPrimary")
    Boolean isPrimary;

    @Property("certificationDate")
    LocalDate certificationDate;

    @Property("certificationBody")
    String certificationBody;

    @Property("yearsOfExperienceInSpecialty")
    Integer yearsOfExperienceInSpecialty;
}
