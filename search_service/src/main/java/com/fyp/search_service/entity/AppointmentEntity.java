package com.fyp.search_service.entity;


import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.time.LocalDateTime;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Document(indexName = "appointment")
public class AppointmentEntity {
    @Id
    String id;
    String patientName;
    String doctorName;
    String doctorSpecialty;
    LocalDateTime appointmentDate;
    String status;
    String reason;




}
