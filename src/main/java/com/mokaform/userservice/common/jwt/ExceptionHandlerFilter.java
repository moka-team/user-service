package com.mokaform.userservice.common.jwt;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mokaform.userservice.common.exception.AuthException;
import com.mokaform.userservice.common.exception.errorcode.CommonErrorCode;
import com.mokaform.userservice.common.exception.errorcode.ErrorCode;
import com.mokaform.userservice.common.exception.response.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class ExceptionHandlerFilter extends OncePerRequestFilter {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            filterChain.doFilter(request, response);
        } catch (TokenExpiredException e) {
            log.warn("AccessToken 유효기한 만료되었습니다. {}", e.getMessage());
            setErrorResponse(response, CommonErrorCode.EXPIRED_ACCESS_TOKEN);
        } catch (AuthException e) {
            log.warn("AccessToken 로그아웃 처리되었습니다. {}", e.getMessage());
            setErrorResponse(response, e.getErrorCode());
        } catch (JWTVerificationException e) {
            log.warn("Jwt processing failed: {}", e.getMessage());
            setErrorResponse(response, CommonErrorCode.ILLEGAL_TOKEN);
        }
    }

    public void setErrorResponse(HttpServletResponse response, ErrorCode errorCode)
            throws IOException {
        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType("application/json");
        response.setContentType("text/plain;charset=UTF-8");
        ErrorResponse exceptionResponse = ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(exceptionResponse);
        response.getWriter().write(json);
    }
}
