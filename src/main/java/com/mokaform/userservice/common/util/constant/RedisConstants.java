package com.mokaform.userservice.common.util.constant;

public enum RedisConstants {
    LOGOUT("auth:logout:access-token:"),
    LOGIN("auth:login:refresh-token:"),
    EMAIL_VERIFICATION("auth:email-verification");

    private final String prefix;

    RedisConstants(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }
}
