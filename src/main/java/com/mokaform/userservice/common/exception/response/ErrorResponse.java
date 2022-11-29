package com.mokaform.userservice.common.exception.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.springframework.validation.FieldError;

import java.util.List;

@Builder
@Getter
public class ErrorResponse {
    private final String code;

    private final String message;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final List<ValidationError> errors;

    public ErrorResponse(String code, String message, List<ValidationError> errors) {
        this.code = code;
        this.message = message;
        this.errors = errors;
    }

    @Builder
    @Getter
    public static class ValidationError {
        private final String field;
        
        private final String message;

        public ValidationError(String field, String message) {
            this.field = field;
            this.message = message;
        }

        public static ValidationError of(final FieldError fieldError) {
            return ValidationError.builder()
                    .field(fieldError.getField())
                    .message(fieldError.getDefaultMessage())
                    .build();
        }
    }
}
