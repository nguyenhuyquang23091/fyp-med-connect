package com.fyp.video_call_service.exceptions;


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
    APPOINTMENT_NOT_OWNED(2008, "You do not have permission to modify this appointment", HttpStatus.FORBIDDEN),
    APPOINTMENT_ALREADY_CANCELLED(2009, "Appointment is already cancelled", HttpStatus.BAD_REQUEST),
    APPOINTMENT_ALREADY_COMPLETED(2010, "Cannot cancel a completed appointment", HttpStatus.BAD_REQUEST)
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
