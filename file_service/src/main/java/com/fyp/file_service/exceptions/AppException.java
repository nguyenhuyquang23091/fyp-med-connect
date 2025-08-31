package com.fyp.file_service.exceptions;



import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppException extends  RuntimeException {
    private ErrorCode errorCode;
    public AppException(ErrorCode errorCode) {
        //This super constructor is used for the "message" parameter in RuntimeException//
        //And it's default message
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
