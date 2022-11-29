package com.mokaform.userservice.common.jwt;

import com.mokaform.userservice.common.util.RedisService;
import com.mokaform.userservice.common.util.constant.RedisConstants;
import com.mokaform.userservice.domain.User;
import com.mokaform.userservice.service.UserService;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;

import java.text.MessageFormat;
import java.time.Duration;
import java.util.List;

import static org.apache.commons.lang3.ClassUtils.isAssignable;

public class JwtAuthenticationProvider implements AuthenticationProvider {

    private final Jwt jwt;

    private final UserService userService;

    private final RedisService redisService;

    public JwtAuthenticationProvider(Jwt jwt, UserService userService, RedisService redisService) {
        this.jwt = jwt;
        this.userService = userService;
        this.redisService = redisService;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return isAssignable(JwtAuthenticationToken.class, authentication);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        JwtAuthenticationToken jwtAuthentication = (JwtAuthenticationToken) authentication;
        return processUserAuthentication(
                String.valueOf(jwtAuthentication.getPrincipal()),
                jwtAuthentication.getCredentials()
        );
    }

    private Authentication processUserAuthentication(String principal, String credentials) {
        try {
            User user = userService.login(principal, credentials);
            List<GrantedAuthority> authorities = user.getAuthorities();
            String accessToken = getAccessToken(user.getEmail(), authorities);
            String refreshToken = getRefreshToken(user.getEmail(), authorities);
            JwtAuthenticationToken authenticated =
                    new JwtAuthenticationToken(new JwtAuthentication(accessToken, user.getEmail()), null, authorities);
            authenticated.setDetails(refreshToken);
            return authenticated;
        } catch (IllegalArgumentException e) {
            throw new BadCredentialsException(e.getMessage());
        } catch (DataAccessException e) {
            throw new AuthenticationServiceException(e.getMessage(), e);
        }
    }

    private String getAccessToken(String email, List<GrantedAuthority> authorities) {
        String[] roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .toArray(String[]::new);
        return jwt.signAccessToken(Jwt.Claims.from(email, roles));
    }

    private String getRefreshToken(String email, List<GrantedAuthority> authorities) {
        String[] roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .toArray(String[]::new);
        String token = jwt.signRefreshToken(Jwt.Claims.from(email, roles));
        saveToken(MessageFormat
                        .format("{0}{1}",
                                RedisConstants.LOGIN.getPrefix(),
                                email),
                token,
                jwt.getRefreshTokenExpirySeconds());
        return token;
    }

    private void saveToken(String key, String token, int expirySeconds) {
        redisService.setValues(key, token, Duration.ofSeconds(expirySeconds));
    }

}