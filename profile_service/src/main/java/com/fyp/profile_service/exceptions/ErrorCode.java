package com.fyp.profile_service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.Getter;

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
    REQUEST_NOTFOUND(1008, "Request is not existed ", HttpStatus.BAD_REQUEST),
    REQUEST_ALREADY_PROCESSED(1009, "Request is already processed ", HttpStatus.BAD_REQUEST),
    PRESCRIPTION_NOT_FOUND(1010, "Prescription is not existed", HttpStatus.BAD_REQUEST),
    AVATAR_NOT_FOUND(1011, "Avatar not found", HttpStatus.NOT_FOUND),
    SPECIALTY_NOT_FOUND(1012, "Specialty not found", HttpStatus.NOT_FOUND),
    SPECIALTY_CODE_EXISTED(1013, "Specialty code already exists", HttpStatus.BAD_REQUEST),
    SPECIALTY_NAME_EXISTED(1014, "Specialty name already exists", HttpStatus.BAD_REQUEST),
    MEDICAL_SERVICE_NOT_FOUND(1015, "Medical service not found", HttpStatus.NOT_FOUND),
    MEDICAL_SERVICE_NAME_EXISTED(1016, "Medical service name already exists", HttpStatus.BAD_REQUEST),
    PRACTICE_EXPERIENCE_NOT_FOUND(1017, "Practice experience not found", HttpStatus.NOT_FOUND),
    PRACTICE_EXPERIENCE_NOT_OWNED(1018, "You do not own this practice experience", HttpStatus.FORBIDDEN),
    MEDICAL_SERVICE_ALREADY_ADDED(
            1019, "This medical service is already added to your profile", HttpStatus.BAD_REQUEST),
    DOCTOR_SERVICE_NOT_FOUND(1020, "Doctor service relationship not found", HttpStatus.NOT_FOUND),
    SPECIALTY_ALREADY_ADDED(1021, "This specialty is already added to your profile", HttpStatus.BAD_REQUEST),
    DOCTOR_SPECIALTY_NOT_FOUND(1022, "Doctor specialty relationship not found", HttpStatus.NOT_FOUND);
    private int code;
    private HttpStatusCode statusCode;
    private String message;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
