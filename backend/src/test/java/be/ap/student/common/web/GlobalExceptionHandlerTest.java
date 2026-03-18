package be.ap.student.common.web;

import be.ap.student.common.api.ApiError;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static be.ap.student.common.web.CorrelationIdFilter.MDC_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private MethodArgumentNotValidException methodArgumentNotValidException;

    @Mock
    private BindingResult bindingResult;

    private MockedStatic<MDC> mdcMockedStatic;

    @BeforeEach
    void setUp() {
        mdcMockedStatic = mockStatic(MDC.class);
        mdcMockedStatic.when(() -> MDC.get(MDC_KEY)).thenReturn("test-correlation-id");
    }

    @AfterEach
    void tearDown() {
        mdcMockedStatic.close();
    }

    @Test
    void handleValidation_returnsBadRequestWithFieldErrors() {
        FieldError fieldError = new FieldError("objectName", "field", "default message");
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ApiError> response = globalExceptionHandler.handleValidation(methodArgumentNotValidException);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCorrelationId()).isEqualTo("test-correlation-id");
        assertThat(response.getBody().getCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getBody().getMessage()).isEqualTo("Request validation failed");
        assertThat(response.getBody().getFieldErrors()).hasSize(1);
        assertThat(response.getBody().getFieldErrors().get(0).field()).isEqualTo("field");
        assertThat(response.getBody().getFieldErrors().get(0).message()).isEqualTo("invalid");
    }

    @Test
    void handleConstraintViolation_returnsBadRequest() {
        ConstraintViolationException ex = new ConstraintViolationException("Constraint violation message", null);

        ResponseEntity<ApiError> response = globalExceptionHandler.handleConstraintViolation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCorrelationId()).isEqualTo("test-correlation-id");
        assertThat(response.getBody().getCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getBody().getMessage()).isEqualTo("Constraint violation message");
        assertThat(response.getBody().getFieldErrors()).isEmpty();
    }

    @Test
    void handleIllegalArg_returnsBadRequest() {
        IllegalArgumentException ex = new IllegalArgumentException("Illegal argument message");

        ResponseEntity<ApiError> response = globalExceptionHandler.handleIllegalArg(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCorrelationId()).isEqualTo("test-correlation-id");
        assertThat(response.getBody().getCode()).isEqualTo("BAD_REQUEST");
        assertThat(response.getBody().getMessage()).isEqualTo("Illegal argument message");
        assertThat(response.getBody().getFieldErrors()).isEmpty();
    }

    @Test
    void handleGeneric_returnsInternalServerError() {
        Exception ex = new Exception("Generic error message");

        ResponseEntity<ApiError> response = globalExceptionHandler.handleGeneric(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCorrelationId()).isEqualTo("test-correlation-id");
        assertThat(response.getBody().getCode()).isEqualTo("INTERNAL_ERROR");
        assertThat(response.getBody().getMessage()).isEqualTo("Something went wrong");
        assertThat(response.getBody().getFieldErrors()).isEmpty();
    }
}
