package com.mokaform.userservice.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI(@Value("${springdoc.version}") String appVersion) {
        Info info = new Info().title("Demo API").version(appVersion)
                .description("Team MOKA의 mokaform 웹 애플리케이션 API입니다.\n" +
                        "로그인 요청 후에 응답 헤더(authorization: Bearer ${ACCESS_TOKE})으로 온 accessToken을 오른쪽 Authorize 버튼을 누르고 입력한 후에 API 요청을 보내면" +
                        " 인가가 필요한 요청에 대한 응답을 받을 수 있습니다. 브라우저 보안 정책으로 인해 쿠키 인증은 불가능하여 '토큰 재발행'API는 swagger에서 테스트 불가능합니다.");

        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("Access Token", getAccessTokenAuthScheme())
                        .addSecuritySchemes("Refresh Token", getRefreshTokenAuthScheme()))
                .addSecurityItem(getSecurityItem())
                .info(info);
    }

    private SecurityScheme getAccessTokenAuthScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");
    }

    private SecurityScheme getRefreshTokenAuthScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.COOKIE)
                .name("refreshToken");
    }

    private SecurityRequirement getSecurityItem() {
        SecurityRequirement securityItem = new SecurityRequirement();
        securityItem.addList("Access Token");
        securityItem.addList("Refresh Token");
        return securityItem;
    }
}
