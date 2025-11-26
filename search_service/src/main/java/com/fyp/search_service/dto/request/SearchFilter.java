package com.fyp.search_service.dto.request;

import co.elastic.clients.elasticsearch._types.SortOrder;
import com.fyp.search_service.constant.PredefinedType;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SearchFilter {

    // Autocomplete term
    @Size(min = 2, max = 100, message = "Search term must be between 2 and 100 characters")
    String term;


    @Builder.Default
    List<String> searchFields = PredefinedType.DEFAULT_SEARCH_FIELDS;

    // Boolean filters
    Boolean isAvailable;

    @NotEmpty(message = "Languages list cannot be empty if provided")
    List<@NotBlank(message = "Language code cannot be blank") String> languages;

    // Range filters - Experience
    @Min(value = 0, message = "Minimum years of experience cannot be negative")
    @Max(value = 100, message = "Minimum years of experience cannot exceed 100")
    Integer minYearsOfExperience;

    @Min(value = 0, message = "Maximum years of experience cannot be negative")
    @Max(value = 100, message = "Maximum years of experience cannot exceed 100")
    Integer maxYearsOfExperience;

    // Range filters - Price (nested in services)
    @DecimalMin(value = "0.0", inclusive = true, message = "Minimum price must be positive or zero")
    @Digits(integer = 10, fraction = 2, message = "Price must have at most 10 integer digits and 2 decimal places")
    BigDecimal minPrice;

    @DecimalMin(value = "0.0", inclusive = true, message = "Maximum price must be positive or zero")
    @Digits(integer = 10, fraction = 2, message = "Price must have at most 10 integer digits and 2 decimal places")
    BigDecimal maxPrice;

    // Nested filters
    @NotEmpty(message = "Specialty IDs list cannot be empty if provided")
    List<@NotNull(message = "Specialty ID cannot be null") @Positive(message = "Specialty ID must be positive") Long> specialtyIds;

    @Size(min = 2, max = 100, message = "Country must be between 2 and 100 characters")
    String country;

    @Size(min = 2, max = 200, message = "Location must be between 2 and 200 characters")
    String location;

    // Pagination
    @Builder.Default
    @Min(value = 0, message = "Page number cannot be negative")
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


    @AssertTrue(message = "Maximum years of experience must be greater than or equal to minimum")
    private boolean isYearsOfExperienceRangeValid() {
        if (minYearsOfExperience == null || maxYearsOfExperience == null) {
            return true; // Skip validation if either is null
        }
        return maxYearsOfExperience >= minYearsOfExperience;
    }


    @AssertTrue(message = "Maximum price must be greater than or equal to minimum price")
    private boolean isPriceRangeValid() {
        if (minPrice == null || maxPrice == null) {
            return true; // Skip validation if either is null
        }
        return maxPrice.compareTo(minPrice) >= 0;
    }
}
