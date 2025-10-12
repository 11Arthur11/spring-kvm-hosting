package me.parhamziaei.practice.exception.custom.authenticate;

public class TwoFactorSessionExpiredException extends AuthenticationException {
    public TwoFactorSessionExpiredException() {
        super("2FA_SESSION_EXPIRED");
    }
}
