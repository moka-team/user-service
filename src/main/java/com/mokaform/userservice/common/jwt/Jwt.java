package com.mokaform.userservice.common.jwt;

import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public final class Jwt {

    private final String issuer;

    private final String clientSecret;

    private final String accessTokenHeader;

    private final String refreshTokenHeader;

    private final int accessTokenExpirySeconds;

    private final int refreshTokenExpirySeconds;

    private final Algorithm algorithm;

    private final JWTVerifier jwtVerifier;

    public Jwt(String issuer, String clientSecret, String accessTokenHeader, String refreshTokenHeader, int accessTokenExpirySeconds, int refreshTokenExpirySeconds) {
        this.issuer = issuer;
        this.clientSecret = clientSecret;
        this.accessTokenHeader = accessTokenHeader;
        this.refreshTokenHeader = refreshTokenHeader;
        this.accessTokenExpirySeconds = accessTokenExpirySeconds;
        this.refreshTokenExpirySeconds = refreshTokenExpirySeconds;
        this.algorithm = Algorithm.HMAC512(clientSecret);
        this.jwtVerifier = com.auth0.jwt.JWT.require(algorithm)
                .withIssuer(issuer)
                .build();
    }

    public String signAccessToken(Claims claims) {
        return sign(claims, accessTokenExpirySeconds);
    }

    public String signRefreshToken(Claims claims) {
        return sign(claims, refreshTokenExpirySeconds);
    }

    private String sign(Claims claims, int expirySeconds) {
        Date now = new Date();
        JWTCreator.Builder builder = com.auth0.jwt.JWT.create();
        builder.withIssuer(issuer);
        builder.withIssuedAt(now);
        if (expirySeconds > 0) {
            builder.withExpiresAt(new Date(now.getTime() + expirySeconds * 1_000L));
        }
        builder.withClaim("email", claims.email);
        builder.withArrayClaim("roles", claims.roles);
        return builder.sign(algorithm);
    }

    public Claims verify(String token) throws JWTVerificationException {
        return new Claims(jwtVerifier.verify(token));
    }

    public Boolean isExpiredToken(String accessToken) {
        DecodedJWT decodedJWT = com.auth0.jwt.JWT.decode(accessToken);
        return decodedJWT.getExpiresAt().before(new Date());
    }

    public String getIssuer() {
        return issuer;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getAccessTokenHeader() {
        return accessTokenHeader;
    }

    public String getRefreshTokenHeader() {
        return refreshTokenHeader;
    }

    public int getAccessTokenExpirySeconds() {
        return accessTokenExpirySeconds;
    }

    public int getRefreshTokenExpirySeconds() {
        return refreshTokenExpirySeconds;
    }

    public Algorithm getAlgorithm() {
        return algorithm;
    }

    public JWTVerifier getJwtVerifier() {
        return jwtVerifier;
    }

    static public class Claims {
        String email;
        String[] roles;
        Date iat;
        Date exp;

        private Claims() {/*no-op*/}

        Claims(DecodedJWT decodedJWT) {
            Claim email = decodedJWT.getClaim("email");
            if (!email.isNull()) {
                this.email = email.asString();
            }
            Claim roles = decodedJWT.getClaim("roles");
            if (!roles.isNull()) {
                this.roles = roles.asArray(String.class);
            }
            this.iat = decodedJWT.getIssuedAt();
            this.exp = decodedJWT.getExpiresAt();
        }

        public static Claims from(String email, String[] roles) {
            Claims claims = new Claims();
            claims.email = email;
            claims.roles = roles;
            return claims;
        }

        public Map<String, Object> asMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("email", email);
            map.put("roles", roles);
            map.put("iat", iat());
            map.put("exp", exp());
            return map;
        }

        long iat() {
            return iat != null ? iat.getTime() : -1;
        }

        long exp() {
            return exp != null ? exp.getTime() : -1;
        }

        void eraseIat() {
            iat = null;
        }

        void eraseExp() {
            exp = null;
        }

        @Override
        public String toString() {
            return new StringBuilder()
                    .append(MessageFormat.format("email: {0}", email))
                    .append(MessageFormat.format("roles: {0}", Arrays.toString(roles)))
                    .append(MessageFormat.format("iat: {0}", iat))
                    .append(MessageFormat.format("exp: {0}", exp))
                    .toString();
        }
    }
}
