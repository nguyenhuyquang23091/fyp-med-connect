package com.fyp.search_service.exceptions;


import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter

public enum ErrorCode {
    UNIDENTIFIED_EXCEPTION(9999, "Unidentified Error", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_EXISTED(1001, "User is existed", HttpStatus.BAD_REQUEST),

    // this is for @valid annotation
    PASSWORD_INVALID(1003, "Password must be 8 characters long", HttpStatus.BAD_REQUEST),

    INVALID_KEY(1004, "Invalid message", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User is not existed", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),

    // Appointment specific errors
    DOCTOR_NOT_FOUND(2001, "Doctor not found", HttpStatus.NOT_FOUND),
    DOCTOR_NOT_AVAILABLE(2002, "Doctor is not available for appointments", HttpStatus.BAD_REQUEST),
    APPOINTMENT_NOT_FOUND(2003, "Appointment not found", HttpStatus.NOT_FOUND),
    INVALID_APPOINTMENT_DATE(2004, "Appointment date must be in the future", HttpStatus.BAD_REQUEST),
    APPOINTMENT_ALREADY_EXISTS(2005, "You already have an appointment with this doctor on this date", HttpStatus.CONFLICT),
    INVALID_SERVICE_SELECTION(2006, "The selected service is not offered by this doctor", HttpStatus.BAD_REQUEST),
    INVALID_SPECIALTY_SELECTION(2007, "The selected specialty is not offered by this doctor", HttpStatus.BAD_REQUEST),

    // Search service specific errors (3xxx range)
    ELASTICSEARCH_CONNECTION_ERROR(3001, "Failed to connect to Elasticsearch", HttpStatus.SERVICE_UNAVAILABLE),
    ELASTICSEARCH_QUERY_ERROR(3002, "Failed to execute search query", HttpStatus.INTERNAL_SERVER_ERROR),
    ELASTICSEARCH_INDEX_ERROR(3003, "Failed to index document in Elasticsearch", HttpStatus.INTERNAL_SERVER_ERROR),
    ELASTICSEARCH_DELETE_ERROR(3004, "Failed to delete document from Elasticsearch", HttpStatus.INTERNAL_SERVER_ERROR),
    ELASTICSEARCH_UPDATE_ERROR(3005, "Failed to update document in Elasticsearch", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_SEARCH_FILTER(3006, "Invalid search filter parameters", HttpStatus.BAD_REQUEST),
    SEARCH_TIMEOUT(3007, "Search request timed out", HttpStatus.REQUEST_TIMEOUT),
    DOCTOR_PROFILE_NOT_FOUND_IN_INDEX(3008, "Doctor profile not found in search index", HttpStatus.NOT_FOUND),

    // CDC event processing errors (3100 range)
    CDC_EVENT_PROCESSING_ERROR(3101, "Failed to process CDC event", HttpStatus.INTERNAL_SERVER_ERROR),
    CDC_EVENT_DESERIALIZATION_ERROR(3102, "Failed to deserialize CDC event", HttpStatus.BAD_REQUEST),
    CDC_INVALID_OPERATION(3103, "Invalid CDC operation type", HttpStatus.BAD_REQUEST),
    CDC_INVALID_ENTITY_TYPE(3104, "Invalid CDC entity type", HttpStatus.BAD_REQUEST)
    ;
    private int code;
    private HttpStatusCode statusCode;
    private String message;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
