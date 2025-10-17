package com.fyp.profile_service.dto.response;

import java.math.BigDecimal;

import lombok.*;
import lombok.experimental.FieldDefaults;

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
