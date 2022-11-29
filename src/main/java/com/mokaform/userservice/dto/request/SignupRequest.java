package com.mokaform.userservice.dto.request;

import javax.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Getter
public class SignupRequest {

    @NotBlank
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private String nickname;

    @NotBlank
    private String ageGroup;

    @NotBlank
    private String gender;

    @NotBlank
    private String job;

    @NotEmpty
    private List<String> category;

    @Builder
    public SignupRequest(String email, String password,
                         String nickname, String ageGroup,
                         String gender, String job,
                         List<String> category) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.ageGroup = ageGroup;
        this.gender = gender;
        this.job = job;
        this.category = category;
    }
}
