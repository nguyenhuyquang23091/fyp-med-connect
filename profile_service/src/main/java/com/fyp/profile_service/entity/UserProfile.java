package com.fyp.profile_service.entity;

import java.time.LocalDate;

import org.springframework.data.neo4j.core.schema.*;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Node("user_profile")
public class UserProfile {
    @Id
    @GeneratedValue(generatorClass = UUIDStringGenerator.class)
    String id;

    // userId from authservice (single source of truth for user identity)
    @Property("userId")
    String userId;

    @Property("email")
    String email;

    @Property("gender")
    String gender;

    String avatar;
    String firstName;
    String lastName;
    LocalDate dob;
    String city;

    @Relationship(type = "HAS_DOCTOR_PROFILE", direction = Relationship.Direction.OUTGOING)
    DoctorProfile doctorProfile;
}
