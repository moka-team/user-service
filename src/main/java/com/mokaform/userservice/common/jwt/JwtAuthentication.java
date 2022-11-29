package com.mokaform.userservice.common.jwt;

import org.springframework.util.Assert;

import java.text.MessageFormat;

public class JwtAuthentication {

    public final String accessToken;

    public final String email;

    JwtAuthentication(String accessToken, String email) {
        checkArgs(accessToken, email);

        this.accessToken = accessToken;
        this.email = email;
    }

    private void checkArgs(String accessToken, String email) {
        Assert.notNull(accessToken, "accessToken must be provided.");
        Assert.isTrue(accessToken.length() != 0, "accessToken must be provided.");

        Assert.notNull(email, "email must be provided.");
        Assert.isTrue(email.length() != 0, "email must be provided.");
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append(MessageFormat.format("accessToken: {0}", accessToken))
                .append(MessageFormat.format("email: {0}", email))
                .toString();
    }

}