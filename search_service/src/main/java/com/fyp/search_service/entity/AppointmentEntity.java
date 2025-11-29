package com.fyp.search_service.entity;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Mapping;

import java.math.BigDecimal;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Document(indexName = "appointment")
@Mapping(mappingPath = "static/appointment.json")
@JsonIgnoreProperties(ignoreUnknown = true)
public class AppointmentEntity {
    @Id
    String id;

    @Field(type = FieldType.Keyword)
    String userId;

    @Field(type = FieldType.Text)
    String patientFullName;

    @Field(type = FieldType.Text)
    String doctorFullName;

    @Field(type = FieldType.Keyword)
    String doctorId;

    @Field(type = FieldType.Text)
    String reasons;

    @Field(type = FieldType.Keyword)
    String phoneNumber;

    @Field(type = FieldType.Date)
    String appointmentDateTime;

    @Field(type = FieldType.Date)
    String createdDate;

    @Field(type = FieldType.Keyword)
    String specialty;

    @Field(type = FieldType.Text)
    String services;

    @Field(type = FieldType.Keyword)
    String appointmentStatus;

    @Field(type = FieldType.Keyword)
    String consultationType;

    @Field(type = FieldType.Keyword)
    String paymentMethod;

    @Field(type = FieldType.Date)
    String modifiedDate;

    @Field(type = FieldType.Double)
    BigDecimal prices;
}
