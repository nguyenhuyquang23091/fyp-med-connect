package com.fyp.search_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Mapping;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Document(indexName = "users")
@Mapping(mappingPath = "static/user.json")
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserEntity {

    @Id
    String id;

    @Field(type = FieldType.Keyword)
    String email;

    @Field(type = FieldType.Keyword)
    String username;

    @Field(type = FieldType.Text)
    String role;
}
