package com.fyp.search_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Document(indexName = "users")
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserEntity {

    //update to delete later, since we use debezium to capture event changes in authservice, we don't have information about their profile

    @Id
    String id;
    String fullName;
    String email;
    String phoneNumber;
    String role;
    String specialization;
    String profileImageUrl;
}
