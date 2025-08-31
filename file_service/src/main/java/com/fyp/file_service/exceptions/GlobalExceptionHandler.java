package com.fyp.file_service.exceptions;



import com.fyp.file_service.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;




//Same as the class name
//This class is used for all exception in 1 place
@ControllerAdvice
public class GlobalExceptionHandler {

@ExceptionHandler(value = Exception.class)
    ResponseEntity<ApiResponse> handleRuntimeException(RuntimeException  runtimeException){
    ApiResponse apiResponse = new ApiResponse();
    apiResponse.setCode(ErrorCode.UNIDENTIFIED_EXCEPTION.getCode());
    apiResponse.setMessage(ErrorCode.UNIDENTIFIED_EXCEPTION.getMessage());
    return ResponseEntity.badRequest().body(apiResponse);
}
    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse> handleAppException(AppException  appException){
        //Assigning a reference to enum instance, not instantiating new object
        ErrorCode errorCode = appException.getErrorCode();
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());
        return ResponseEntity
                .status(errorCode.getStatusCode())
                .body(apiResponse);
    }
    @ExceptionHandler(value = AccessDeniedException.class)
    ResponseEntity<ApiResponse> handleAccessDeniedException(AccessDeniedException accessDeniedException){
    ErrorCode errorCode = ErrorCode.UNAUTHORIZED;
    return ResponseEntity.status(errorCode.getStatusCode()).body(ApiResponse.builder()
            .code(errorCode.getCode())
            .message(errorCode.getMessage()).build());
    }
@ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse> handleValidationExceptions (MethodArgumentNotValidException notValidException){
    String enumKey = notValidException.getFieldError().getDefaultMessage();
    // fallback value||default value
    ErrorCode errorCode = ErrorCode.INVALID_KEY;
    //try to catch the error in the ErrorCode class
    try{
      ErrorCode specificErrorCode = ErrorCode.valueOf(enumKey);

      errorCode = specificErrorCode;
      enumKey = errorCode.getMessage();
    }
    //If it don't,
    // it will throw the default message from the @Email or @Size annotation from request class,
    // example is UserCreationRq
    catch (IllegalArgumentException e){
    }
    ApiResponse apiResponse = new ApiResponse();
    apiResponse.setCode(errorCode.getCode());
    apiResponse.setMessage(enumKey);
    return ResponseEntity.badRequest().body(apiResponse);
}
}
