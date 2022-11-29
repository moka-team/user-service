package com.mokaform.userservice.common.config;


import com.mokaform.userservice.common.jwt.*;
import com.mokaform.userservice.common.util.RedisService;
import com.mokaform.userservice.domain.enums.RoleName;
import com.mokaform.userservice.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsUtils;

import javax.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final JwtConfig jwtConfig;

    private final ExceptionHandlerFilter exceptionHandlerFilter;

    public WebSecurityConfig(JwtConfig jwtConfig,
                             ExceptionHandlerFilter exceptionHandlerFilter) {
        this.jwtConfig = jwtConfig;
        this.exceptionHandlerFilter = exceptionHandlerFilter;
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, e) -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Object principal = authentication != null ? authentication.getPrincipal() : null;
            log.warn("{} is denied", principal, e);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("ACCESS DENIED");
            response.getWriter().flush();
            response.getWriter().close();
        };
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, e) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("UNAUTHORIZED");
            response.getWriter().flush();
            response.getWriter().close();
        };
    }

    @Bean
    public Jwt jwt() {
        return new Jwt(
                jwtConfig.getIssuer(),
                jwtConfig.getClientSecret(),
                jwtConfig.getAccessTokenHeader(),
                jwtConfig.getRefreshTokenHeader(),
                jwtConfig.getAccessTokenExpirySeconds(),
                jwtConfig.getRefreshTokenExpirySeconds()
        );
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtService jwtService) {
        return new JwtAuthenticationFilter(jwtService);
    }

    @Bean
    public JwtAuthenticationProvider jwtAuthenticationProvider(Jwt jwt,
                                                               UserService userService,
                                                               RedisService redisService) {
        return new JwtAuthenticationProvider(jwt, userService, redisService);
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           JwtAuthenticationFilter jwtAuthenticationFilter,
                                           AuthenticationEntryPoint authenticationEntryPoint) throws Exception {
        http.authorizeRequests()
                .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
                .antMatchers("/api/v1/survey/list",
                        "/api/v1/survey/recommended-list").permitAll()
                .antMatchers("/api/v1/users/my/**",
                        "/api/v1/survey/**",
                        "/api/v1/answer/**").hasAnyAuthority(RoleName.USER.name())
                .anyRequest().permitAll()
                .and()
                /**
                 * formLogin, csrf, headers, http-basic, rememberMe, logout filter 비활성화
                 */
                .formLogin()
                .disable()
                .csrf()
                .disable()
                .headers()
                .disable()
                .httpBasic()
                .disable()
                .rememberMe()
                .disable()
                .logout()
                .disable()
                /**
                 * Session 사용하지 않음
                 */
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                /**
                 * 예외처리 핸들러
                 */
                .exceptionHandling()
                .accessDeniedHandler(accessDeniedHandler())
                .authenticationEntryPoint(authenticationEntryPoint)
                .and()
                /**
                 * FilterChain
                 */
                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(exceptionHandlerFilter,
                        JwtAuthenticationFilter.class);

        return http.build();
    }

}
