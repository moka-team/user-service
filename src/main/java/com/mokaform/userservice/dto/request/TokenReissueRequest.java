package com.mokaform.userservice.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;

@Getter
@NoArgsConstructor
public class TokenReissueRequest {

    @NotBlank
    private String accessToken;

    @NotBlank
    private String refreshToken;

    public TokenReissueRequest(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}