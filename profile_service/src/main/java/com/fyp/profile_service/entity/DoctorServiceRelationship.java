package com.fyp.profile_service.entity;

import java.math.BigDecimal;

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
public class DoctorServiceRelationship {

    @Id
    @GeneratedValue
    Long id;

    @TargetNode
    MedicalService service;

    @Property("price")
    BigDecimal price;

    @Property("currency")
    String currency;

    @Property("displayOrder")
    Integer displayOrder;
}
