package com.project.asset.exception;

import com.project.asset.response.ApiResponse;
import com.project.asset.response.ValidationError;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException ex) {
        log.warn("Business exception: {}", ex.getMessage());
        return ApiResponse.failure(ex.getErrorCode(), ex.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ApiResponse<List<ValidationError>> handleMethodArgumentNotValidException(Exception ex) {
        List<FieldError> fieldErrors;
        if (ex instanceof MethodArgumentNotValidException argumentNotValidException) {
            fieldErrors = argumentNotValidException.getBindingResult().getFieldErrors();
        } else {
            fieldErrors = ((BindException) ex).getBindingResult().getFieldErrors();
        }
        List<ValidationError> errors = fieldErrors.stream()
                .map(error -> ValidationError.builder()
                        .field(error.getField())
                        .message(error.getDefaultMessage())
                        .build())
                .collect(Collectors.toList());
        return ApiResponse.failure(ErrorCode.VALIDATION_FAILED, errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResponse<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.warn("Invalid request payload", ex);
        return ApiResponse.failure(ErrorCode.BAD_REQUEST, "请求体无法解析");
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResponse<List<ValidationError>> handleConstraintViolationException(ConstraintViolationException ex) {
        List<ValidationError> errors = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .map(message -> ValidationError.builder().message(message).build())
                .collect(Collectors.toList());
        return ApiResponse.failure(ErrorCode.VALIDATION_FAILED, errors);
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception ex) {
        log.error("Unhandled exception", ex);
        return ApiResponse.failure(ErrorCode.SERVER_ERROR, "系统异常，请稍后重试");
    }
}


