package com.fyp.file_service.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "file-management")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FileManagement {
    @Id
    String id;
    String ownerId;
    String publicId;
    String secureUrl;
    String originalFilename;
    String resourceType;
    String type;
    String createdAt;
    Integer width;
    Integer height;


}
