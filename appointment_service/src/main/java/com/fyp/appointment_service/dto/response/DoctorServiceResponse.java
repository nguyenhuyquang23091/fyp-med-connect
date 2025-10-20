package com.fyp.appointment_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DoctorServiceResponse {

    Long relationshipId;
    MedicalServiceResponse service;
    BigDecimal price;
    String currency;
    Integer displayOrder;
}
