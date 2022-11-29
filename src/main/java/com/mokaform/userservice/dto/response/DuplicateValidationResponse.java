package com.mokaform.userservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonInclude(NON_NULL)
@Getter
public class DuplicateValidationResponse {

    private final Boolean isDuplicated;

    public DuplicateValidationResponse(Boolean isDuplicated) {
        this.isDuplicated = isDuplicated;
    }
}
