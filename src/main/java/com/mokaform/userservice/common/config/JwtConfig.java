package com.mokaform.userservice.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;

@Component
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtConfig {

    private String issuer;

    private String clientSecret;

    private String accessTokenHeader;

    private String refreshTokenHeader;

    private int accessTokenExpirySeconds;

    private int refreshTokenExpirySeconds;

    @Override
    public String toString() {
        return new StringBuilder()
                .append(MessageFormat.format("issuer: {0}", issuer))
                .append(MessageFormat.format("clientSecret: {0}", clientSecret))
                .append(MessageFormat.format("accessTokenHeader: {0}", accessTokenHeader))
                .append(MessageFormat.format("refreshTokenHeader: {0}", refreshTokenHeader))
                .append(MessageFormat.format("accessTokenExpirySeconds: {0}", accessTokenExpirySeconds))
                .append(MessageFormat.format("refreshTokenExpirySeconds: {0}", refreshTokenExpirySeconds))
                .toString();
    }

}
