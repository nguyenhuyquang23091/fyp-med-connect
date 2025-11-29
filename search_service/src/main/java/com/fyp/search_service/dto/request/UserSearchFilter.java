package com.fyp.search_service.dto.request;

import co.elastic.clients.elasticsearch._types.SortOrder;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserSearchFilter {

    // Text search term (searches username and email)
    @Size(min = 2, max = 100, message = "Search term must be between 2 and 100 characters")
    String term;

    // Filter by role
    String role;

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
}
