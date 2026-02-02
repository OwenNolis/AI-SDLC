package be.ap.student.common.api;

import java.util.List;

public class ApiError {
    private final String correlationId;
    private final String code;
    private final String message;
    private final List<FieldError> fieldErrors;

    public ApiError(String correlationId, String code, String message, List<FieldError> fieldErrors) {
        this.correlationId = correlationId;
        this.code = code;
        this.message = message;
        this.fieldErrors = fieldErrors;
    }

    public String getCorrelationId() { return correlationId; }
    public String getCode() { return code; }
    public String getMessage() { return message; }
    public List<FieldError> getFieldErrors() { return fieldErrors; }

    public record FieldError(String field, String message) {}
}