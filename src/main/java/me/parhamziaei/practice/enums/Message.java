package me.parhamziaei.practice.enums;

public enum Message {

    AUTH_BAD_CREDENTIALS("error.auth.bad_credentials"),
    AUTH_ACCOUNT_DISABLED("error.auth.account_disabled"),
    AUTH_ACCOUNT_NOT_FOUND("error.auth.account_not_found"),
    AUTH_ACCOUNT_LOCKED("error.auth.account_locked"),
    AUTH_ALREADY_LOGGED_IN("error.auth.already_logged_in"),
    AUTH_TWO_FACTOR_INVALID("error.auth.2fa_invalid"),

    AUTH_TWO_FACTOR_VERIFIED("success.auth.2fa_verified"),
    AUTH_TWO_FACTOR_SENT("success.auth.2fa_sent"),
    AUTH_LOGIN_SUCCESS("success.auth.login"),
    AUTH_LOGOUT_SUCCESS("success.auth.logout"),

    REGISTER_SUCCESS("success.register.user_registered"),
    REGISTER_EMAIL_ALREADY_TAKEN("error.register.email_already_taken"),

    SERVER_INTERNAL_ERROR("error.server.internal"),
    SERVER_IO_ERROR("error.server.io"),
    SERVER_VALIDATION_ERROR("error.server.validation"),
    SERVER_RESOURCE_NOT_FOUND("error.server.resource_not_found");

    private final String key;

    Message(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }

}
