package com.mokaform.userservice.common.jwt;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.mokaform.userservice.common.exception.ApiException;
import com.mokaform.userservice.common.exception.AuthException;
import com.mokaform.userservice.common.exception.errorcode.CommonErrorCode;
import com.mokaform.userservice.common.util.CookieUtils;
import com.mokaform.userservice.common.util.RedisService;
import com.mokaform.userservice.common.util.constant.RedisConstants;
import com.mokaform.userservice.dto.request.LocalLoginRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.net.URLDecoder;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
public class JwtService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Jwt jwt;

    private final RedisService redisService;

    private final AuthenticationManager authenticationManager;

    public JwtService(Jwt jwt, RedisService redisService, AuthenticationManager authenticationManager) {
        this.jwt = jwt;
        this.redisService = redisService;
        this.authenticationManager = authenticationManager;
    }

    public Jwt.Claims verifyAccessToken(String accessToken) {
        checkLoggedOut(accessToken);

        return verify(accessToken);
    }

    public void login(@Valid LocalLoginRequest request, HttpServletResponse response) {
        JwtAuthenticationToken authToken = new JwtAuthenticationToken(request.getEmail(), request.getPassword());
        Authentication resultToken = authenticationManager.authenticate(authToken);
        JwtAuthentication authentication = (JwtAuthentication) resultToken.getPrincipal();
        String accessToken = authentication.accessToken;
        String refreshToken = (String) resultToken.getDetails();

        CookieUtils.addJWTToCookie(response, jwt.getRefreshTokenHeader(), refreshToken);
        response.setHeader("Authorization", "Bearer " + accessToken);
    }

    public void logout(String token) {
        Jwt.Claims verifiedClaims = jwt.verify(token);
        long remainingExpirySeconds = verifiedClaims.exp.getTime() - new Date().getTime();
        redisService.setValues(RedisConstants.LOGOUT.getPrefix() + token, token, Duration.ofMillis(remainingExpirySeconds));
    }

    public void reissueAccessToken(HttpServletRequest request, HttpServletResponse response) {
        String accessToken = getAccessToken(request)
                .orElseThrow(() -> new ApiException(CommonErrorCode.ACCESS_TOKEN_NOT_EXIST));
        if (!jwt.isExpiredToken(accessToken)) {
            throw new ApiException(CommonErrorCode.NOT_EXPIRED_ACCESS_TOKEN);
        }

        Cookie cookie = CookieUtils.getCookie(request, jwt.getRefreshTokenHeader())
                .orElseThrow(() -> new ApiException(CommonErrorCode.REFRESH_TOKEN_NOT_EXIST));
        String refreshToken = cookie.getValue();

        Jwt.Claims claims = getClaims(accessToken);
        checkRefreshToken(claims.email, refreshToken);
        String newAccessToken = getNewAccessToken(claims.email, claims.roles);
        response.setHeader("Authorization", "Bearer " + newAccessToken);
    }

    public Jwt.Claims getAccessTokenClaims(String accessToken) {
        checkLoggedOut(accessToken);
        return getClaims(accessToken);
    }

    public void checkLoggedOut(String accessToken) {
        String values = redisService.getValues(RedisConstants.LOGOUT.getPrefix() + accessToken);
        if (values != null) {
            throw new AuthException(CommonErrorCode.LOGGED_OUT_ACCESS_TOKEN);
        }
    }

    public boolean isRequiredAuthorization(String requestURI) {
        String[] notRequiredAuth = {
                "/api/v1/users/signup",
                "/api/v1/users/check-email-duplication",
                "/api/v1/users/check-nickname-duplication",
                "/api/v1/users/login",
                "/api/v1/users/token/reissue",
                "/api/v1/users/signup/email-verification/send",
                "/api/v1/users/signup/email-verification/check",
                "/api/v1/users/reset-password/email-verification/send",
                "/api/v1/users/reset-password/email-verification/check",
                "/api/v1/users/reset-password",
                "/api/v1/survey/list",
                "/api/v1/survey/recommended-list"
        };

        return !Arrays.asList(notRequiredAuth).contains(requestURI);
    }

    private void checkRefreshToken(String email, String refreshToken) {
        String redisToken = redisService.getValues(MessageFormat.format("{0}{1}",
                RedisConstants.LOGIN.getPrefix(), email));
        if (redisToken == null || !refreshToken.equals(redisToken)) {
            throw new ApiException(CommonErrorCode.ILLEGAL_REFRESH_TOKEN);
        }
    }

    private String getNewAccessToken(String email, String[] roles) {
        return jwt.signAccessToken(Jwt.Claims.from(email, roles));
    }

    private Jwt.Claims getClaims(String token) {
        DecodedJWT decodedJWT = com.auth0.jwt.JWT.decode(token);
        return new Jwt.Claims(decodedJWT);
    }

    private Jwt.Claims verify(String token) {
        return jwt.verify(token);
    }

    private Optional<String> getAccessToken(HttpServletRequest request) {
        String token = getJwtFromRequest(request);
        if (isNotBlank(token)) {
            log.debug("Jwt access token detected: {}", token);
            try {
                return Optional.of(URLDecoder.decode(token, "UTF-8"));
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        return Optional.empty();
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (isNotBlank(bearerToken)
                && bearerToken.startsWith("Bearer")) {
            log.debug("Bearer Token detected: {}", bearerToken);
            return bearerToken.substring("Bearer ".length());
        }
        return null;
    }
}
