package com.fyp.search_service.mapper;

import com.fyp.search_service.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Map;

/**
 * MapStruct mapper for converting CDC events from Debezium to UserEntity.
 * Maps PostgreSQL snake_case column names to Java camelCase field names.
 * Explicitly excludes password field for security.
 */
@Mapper(componentModel = "spring")
public interface UserCdcMapper {

    @Mapping(target = "id", expression = "java(toString(cdcData.get(\"id\")))")
    @Mapping(target = "email", expression = "java(toString(cdcData.get(\"email\")))")
    @Mapping(target = "username", expression = "java(toString(cdcData.get(\"username\")))")
    @Mapping(target = "role", expression = "java(extractRoles(cdcData))")
    UserEntity toUserEntity(Map<String, Object> cdcData);

    /**
     * Converts an object to String, handling null values.
     *
     * @param obj the object to convert
     * @return string representation or null
     */
    default String toString(Object obj) {
        return obj != null ? obj.toString() : null;
    }

    /**
     * Extracts and formats roles from CDC data.
     * Handles various formats: single role string, array, or Set of Role objects.
     * Note: Password field is intentionally excluded from mapping.
     *
     * @param cdcData the CDC event data map
     * @return comma-separated role names or null
     */
    @Named("extractRoles")
    default String extractRoles(Map<String, Object> cdcData) {
        Object rolesObj = cdcData.get("roles");

        if (rolesObj == null) {
            return null;
        }

        // Reason: Handle role data as string if it's already formatted
        if (rolesObj instanceof String) {
            return (String) rolesObj;
        }

        // Reason: CDC might send roles as JSON string, return as-is
        return rolesObj.toString();
    }
}