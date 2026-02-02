package be.ap.student.common.web;

import be.ap.student.common.api.ApiError;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

import static be.ap.student.common.web.CorrelationIdFilter.MDC_KEY;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        String correlationId = MDC.get(MDC_KEY);
        List<ApiError.FieldError> fields = ex.getBindingResult().getFieldErrors().stream()
                .map(this::mapFieldError)
                .toList();
        ApiError body = new ApiError(correlationId, "VALIDATION_ERROR", "Request validation failed", fields);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex) {
        String correlationId = MDC.get(MDC_KEY);
        ApiError body = new ApiError(correlationId, "VALIDATION_ERROR", ex.getMessage(), List.of());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArg(IllegalArgumentException ex) {
        String correlationId = MDC.get(MDC_KEY);
        ApiError body = new ApiError(correlationId, "BAD_REQUEST", ex.getMessage(), List.of());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex) {
        String correlationId = MDC.get(MDC_KEY);
        ApiError body = new ApiError(correlationId, "INTERNAL_ERROR", "Something went wrong", List.of());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private ApiError.FieldError mapFieldError(FieldError fe) {
        return new ApiError.FieldError(fe.getField(), fe.getDefaultMessage());
    }
}
