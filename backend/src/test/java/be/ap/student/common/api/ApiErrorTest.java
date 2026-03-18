package be.ap.student.common.api;

import org.junit.jupiter.api.Test;

import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

public class ApiErrorTest {

    @Test
    void apiError_constructorAndGetters_workCorrectly() {
        String correlationId = "corr-123";
        String code = "TEST_CODE";
        String message = "Test message";
        ApiError.FieldError fieldError1 = new ApiError.FieldError("field1", "message1");
        ApiError.FieldError fieldError2 = new ApiError.FieldError("field2", "message2");
        List<ApiError.FieldError> fieldErrors = List.of(fieldError1, fieldError2);

        ApiError apiError = new ApiError(correlationId, code, message, fieldErrors);

        assertThat(apiError.getCorrelationId()).isEqualTo(correlationId);
        assertThat(apiError.getCode()).isEqualTo(code);
        assertThat(apiError.getMessage()).isEqualTo(message);
        assertThat(apiError.getFieldErrors()).isEqualTo(fieldErrors);
        assertThat(apiError.getFieldErrors()).hasSize(2);
    }

    @Test
    void fieldError_constructorAndGetters_workCorrectly() {
        String field = "testField";
        String message = "testMessage";

        ApiError.FieldError fieldError = new ApiError.FieldError(field, message);

        assertThat(fieldError.field()).isEqualTo(field);
        assertThat(fieldError.message()).isEqualTo(message);
    }

    @Test
    void apiError_withEmptyFieldErrors_worksCorrectly() {
        String correlationId = "corr-456";
        String code = "NO_FIELDS";
        String message = "No field errors";
        List<ApiError.FieldError> fieldErrors = List.of();

        ApiError apiError = new ApiError(correlationId, code, message, fieldErrors);

        assertThat(apiError.getCorrelationId()).isEqualTo(correlationId);
        assertThat(apiError.getCode()).isEqualTo(code);
        assertThat(apiError.getMessage()).isEqualTo(message);
        assertThat(apiError.getFieldErrors()).isEmpty();
    }
}
