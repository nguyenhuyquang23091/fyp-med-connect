package com.fyp.search_service.dto.request;

import co.elastic.clients.elasticsearch._types.SortOrder;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AppointmentSearchFilter {

    // Text search term
    @Size(min = 2, max = 100, message = "Search term must be between 2 and 100 characters")
    String term;

    // Filter by user
    String userId;

    // Filter by doctor
    String doctorId;

    // Filter by appointment status
    String appointmentStatus;

    // Filter by consultation type
    String consultationType;

    // Filter by payment method
    String paymentMethod;

    // Filter by specialty
    String specialty;

    // Date range filter
    LocalDateTime startDate;

    LocalDateTime endDate;

    // Pagination
    @Builder.Default
    @Min(value = 1, message = "Page number must be at least 1")
    @Max(value = 10000, message = "Page number cannot exceed 10000")
    Integer page = 1;

    @Builder.Default
    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size cannot exceed 100")
    Integer size = 10;

    // Sorting
    @Pattern(regexp = "^[a-zA-Z._]+$", message = "Sort field must contain only letters, dots, and underscores")
    @Size(max = 50, message = "Sort field name cannot exceed 50 characters")
    String sortBy;

    SortOrder sortOrder;

    @AssertTrue(message = "End date must be after start date")
    private boolean isDateRangeValid() {
        if (startDate == null || endDate == null) {
            return true;
        }
        return endDate.isAfter(startDate) || endDate.isEqual(startDate);
    }
}
