package com.mokaform.userservice.dto.request;

import javax.validation.constraints.NotBlank;
import lombok.Getter;


@Getter
public class ResetPasswordRequest {

    @NotBlank
    private String email;

    @NotBlank
    private String password;

    public ResetPasswordRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
