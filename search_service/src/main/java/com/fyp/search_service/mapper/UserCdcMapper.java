package com.fyp.search_service.mapper;

import com.fyp.search_service.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Map;


@Mapper(componentModel = "spring")
public interface UserCdcMapper {


    @Mapping(target = "id", expression = "java(toString(cdcData.get(\"id\")))")
    @Mapping(target = "fullName", expression = "java(buildFullName(cdcData))")
    @Mapping(target = "email", expression = "java(toString(cdcData.get(\"email\")))")
    @Mapping(target = "phoneNumber", expression = "java(toString(cdcData.get(\"phone_number\")))")
    @Mapping(target = "role", expression = "java(toString(cdcData.get(\"role\")))")
    @Mapping(target = "specialization", expression = "java(toString(cdcData.get(\"specialization\")))")
    @Mapping(target = "profileImageUrl", expression = "java(toString(cdcData.get(\"profile_image_url\")))")
    UserEntity toUserEntity(Map<String, Object> cdcData);

    /**
     * Safely converts Object to String, handling null values.
     *
     * @param obj the object to convert to String
     * @return String representation of the object, or null if input is null
     */
    default String toString(Object obj) {
        return obj != null ? obj.toString() : null;
    }

    @Named("buildFullName")
    default String buildFullName(Map<String, Object> cdcData) {
        String firstName = (String) cdcData.get("first_name");
        String lastName = (String) cdcData.get("last_name");

        if (firstName == null && lastName == null) {
            return "";
        } else if (firstName == null) {
            return lastName;
        } else if (lastName == null) {
            return firstName;
        } else {
            return firstName + " " + lastName;
        }
    }
}