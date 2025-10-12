package com.fyp.notification_service.exceptions;


import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter


public enum ErrorCode {
    UNIDENTIFIED_EXCEPTION(9999, "Unidentified Error", HttpStatus.INTERNAL_SERVER_ERROR),

    // this is for @valid annotation

    INVALID_KEY(1004, "Invalid message", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User is not existed", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN ),
    CAN_NOT_SEND_EMAIL(1009, "Can't not send email", HttpStatus.BAD_REQUEST ),

    // Notification validation errors
    RECIPIENT_ID_NULL(1010, "Recipient ID cannot be null", HttpStatus.BAD_REQUEST),
    RECIPIENT_ID_EMPTY(1011, "Recipient ID cannot be empty", HttpStatus.BAD_REQUEST),
    NOTIFICATION_TYPE_NULL(1012, "Notification type cannot be null", HttpStatus.BAD_REQUEST),
    NOTIFICATION_NOT_EXIST(1013, "Notification doesn't exist", HttpStatus.BAD_REQUEST),




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
