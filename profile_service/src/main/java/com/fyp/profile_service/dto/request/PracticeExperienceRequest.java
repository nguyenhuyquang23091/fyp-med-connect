package com.fyp.profile_service.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.*;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PracticeExperienceRequest {

    @NotBlank(message = "Hospital name is required")
    @Size(min = 2, max = 200, message = "Hospital name must be between 2 and 200 characters")
    String hospitalName;

    @Size(max = 500, message = "Hospital logo URL must not exceed 500 characters")
    @Pattern(regexp = "^(https?://.*|)$", message = "Hospital logo must be a valid HTTP/HTTPS URL or empty")
    String hospitalLogo;

    @NotBlank(message = "Department is required")
    @Size(min = 2, max = 150, message = "Department must be between 2 and 150 characters")
    String department;

    @NotBlank(message = "Location is required")
    @Size(min = 2, max = 150, message = "Location must be between 2 and 150 characters")
    String location;

    @NotBlank(message = "Country is required")
    @Size(min = 2, max = 100, message = "Country must be between 2 and 100 characters")
    String country;

    @NotBlank(message = "Position is required")
    @Size(min = 2, max = 150, message = "Position must be between 2 and 150 characters")
    String position;

    @NotNull(message = "Start date is required")
    @PastOrPresent(message = "Start date cannot be in the future")
    LocalDate startDate;

    @PastOrPresent(message = "End date cannot be in the future")
    LocalDate endDate;

    @NotNull(message = "Current employment status is required")
    Boolean isCurrent;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    String description;

    @Min(value = 0, message = "Display order must be non-negative")
    @Max(value = 100, message = "Display order must not exceed 100")
    Integer displayOrder;

    @AssertTrue(message = "End date is required when position is not current")
    private boolean isEndDateValid() {
        if (isCurrent != null && !isCurrent) {
            return endDate != null;
        }
        return true;
    }

    @AssertTrue(message = "End date must be after start date")
    private boolean isDateRangeValid() {
        if (startDate != null && endDate != null) {
            return !endDate.isBefore(startDate);
        }
        return true;
    }
}
