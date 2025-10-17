package com.fyp.profile_service.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.*;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DoctorServiceRequest {

    @NotBlank(message = "Service ID is required")
    String serviceId;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Price must have at most 10 integer digits and 2 decimal places")
    BigDecimal price;

    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid 3-letter ISO code (e.g., VND, USD)")
    String currency;

    @Min(value = 0, message = "Display order must be non-negative")
    @Max(value = 100, message = "Display order must not exceed 100")
    Integer displayOrder;
}
