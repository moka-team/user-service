package com.mokaform.userservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonInclude(NON_NULL)
@Getter
public class LocalLoginResponse {

    private final String accessToken;

    private final String refreshToken;

    private final String email;

    public LocalLoginResponse(String accessToken, String refreshToken, String username) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.email = username;
    }
}
