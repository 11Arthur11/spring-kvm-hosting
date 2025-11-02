package me.parhamziaei.practice.service;

import lombok.RequiredArgsConstructor;
import me.parhamziaei.practice.entity.redis.EmailVerifySession;
import me.parhamziaei.practice.entity.redis.TwoFactorSession;
import me.parhamziaei.practice.exception.custom.authenticate.InvalidEmailVerifyCodeException;
import me.parhamziaei.practice.exception.custom.authenticate.InvalidTwoFactorException;
import me.parhamziaei.practice.repository.redis.EmailVerifyRepo;
import me.parhamziaei.practice.repository.redis.TwoFactorRepo;
import me.parhamziaei.practice.util.SecurityUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TwoFactorService {

    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final TwoFactorRepo twoFactorRepo;
    private final EmailService emailService;
    private final EmailVerifyRepo emailVerifyRepo;

    public TwoFactorSession verifyAndGetSession(String twoFactorJwt, String code) {
        String sessionId = jwtService.extractSessionIdFromTwoFactorToken(twoFactorJwt);
        TwoFactorSession session = twoFactorRepo.get(sessionId);
        if (jwtService.isTwoFactorTokenValid(twoFactorJwt)) {
            if (session != null) {
                if (
                        encoder.matches(code, session.getCode()) &&
                        session.getAttempts() < 3 &&
                        session.getUserEmail().equals(jwtService.extractUsername(twoFactorJwt))
                ) {
                    session.setVerified(true);
                    twoFactorRepo.remove(sessionId);
                    return session;
                } else {
                    session.setAttempts(session.getAttempts() + 1);
                    twoFactorRepo.save(sessionId, session);
                }
            } else {
                throw new InvalidTwoFactorException();
            }
        }
        return session;
    }

    public boolean hasActiveTwoFactorSession(String sessionId) {
        return twoFactorRepo.get(sessionId) != null;
    }
    public boolean hasActiveEmailVerifySession(String sessionId) {
        return emailVerifyRepo.get(sessionId) != null;
    }

    public String sendEmailVerificationCode(String email) {
        String sessionId = UUID.randomUUID().toString();
        SecureRandom random = new SecureRandom();
        String code = String.format("%06d", random.nextInt(1000000));
        emailService.sendEmailVerificationEmail(email, code);
        EmailVerifySession session = EmailVerifySession.builder()
                .userEmail(email)
                .code(encoder.encode(code))
                .attempts(0)
                .build();

        emailVerifyRepo.save(sessionId, session, SecurityUtil.EMAIL_VERIFY_SESSION_TTL);
        return sessionId;
    }

    public boolean isVerificationCodeCorrect(String twoFactorJwt, String code) {
        if (jwtService.isTwoFactorTokenValid(twoFactorJwt)) {
            final String sessionId = jwtService.extractSessionIdFromTwoFactorToken(twoFactorJwt);
            final EmailVerifySession session = emailVerifyRepo.get(sessionId);
            if (session != null) {
                if (
                        encoder.matches(code, session.getCode()) &&
                        session.getAttempts() < 3 &&
                        session.getUserEmail().equals(jwtService.extractUsername(twoFactorJwt))
                ) {
                    emailVerifyRepo.remove(sessionId);
                    return true;
                } else {
                    session.setAttempts(session.getAttempts() + 1);
                    emailVerifyRepo.save(sessionId, session);
                }
            }
        }
        throw new InvalidEmailVerifyCodeException();
    }

    public String sendTwoFactor(String email, boolean isRememberMe) {
        SecureRandom random = new SecureRandom();
        TwoFactorSession session = TwoFactorSession.builder()
                .code(String.format("%04d", random.nextInt(10000)))
                .userEmail(email)
                .rememberMe(isRememberMe)
                .verified(false)
                .build();

        String sessionId = UUID.randomUUID().toString();
        emailService.sendTwoFactorCodeEmail(session.getUserEmail(), session.getCode());
        session.setCode(encoder.encode(session.getCode()));
        twoFactorRepo.save(sessionId, session, SecurityUtil.TWO_FACTOR_SESSION_TTL);
        return sessionId;
    }

}
