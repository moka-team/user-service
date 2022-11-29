package com.mokaform.userservice.common.jwt;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.mokaform.userservice.common.exception.AuthException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        if (!CorsUtils.isPreFlightRequest(request)) {
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                getAccessToken(request).ifPresent(token -> {
                    if (jwtService.isRequiredAuthorization(request.getRequestURI())) {
                        try {
                            Jwt.Claims claims = jwtService.verifyAccessToken(token);
                            log.debug("Jwt parse result: {}", claims);

                            String email = claims.email;
                            List<GrantedAuthority> authorities = getAuthorities(claims);

                            if (StringUtils.hasText(email) && authorities.size() > 0) {
                                JwtAuthenticationToken authentication =
                                        new JwtAuthenticationToken(new JwtAuthentication(token, email), null, authorities);
                                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                                SecurityContextHolder.getContext().setAuthentication(authentication);
                            }
                        } catch (TokenExpiredException e) {
                            log.warn("만료된 토큰입니다. {}", e.getMessage());
                            throw e;
                        } catch (AuthException e) {
                            log.warn("로그아웃 처리된 토큰입니다. {}", e.getMessage());
                            throw e;
                        } catch (Exception e) {
                            log.warn("Jwt processing failed: {}", e.getMessage());
                            throw e;
                        }
                    }
                });
            } else {
                log.debug("SecurityContextHolder not populated with security token, as it already contained: '{}'",
                        SecurityContextHolder.getContext().getAuthentication());
            }
        }

        chain.doFilter(request, response);
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

    private List<GrantedAuthority> getAuthorities(Jwt.Claims claims) {
        String[] roles = claims.roles;
        return roles == null || roles.length == 0 ?
                emptyList() :
                Arrays.stream(roles).map(SimpleGrantedAuthority::new).collect(toList());
    }

}