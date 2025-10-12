package com.fyp.file_service.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FileInfo {
    @JsonProperty("public_id")
    String publicId;
    @JsonProperty("secure_url")
    String secureUrl;
    @JsonProperty("original_filename")
    String originalFilename;
    @JsonProperty("resource_type")
    String resourceType;

    @JsonProperty("type")
    String type;

    @JsonProperty("created_at")
    String createdAt;

    @JsonProperty("width")
    Integer width;

    @JsonProperty("height")
    Integer height;



}
