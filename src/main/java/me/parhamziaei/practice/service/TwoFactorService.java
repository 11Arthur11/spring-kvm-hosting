package me.parhamziaei.practice.service;

import lombok.RequiredArgsConstructor;
import me.parhamziaei.practice.entity.TwoFactorSession;
import me.parhamziaei.practice.exception.custom.authenticate.TwoFactorSessionExpiredException;
import me.parhamziaei.practice.repository.TwoFactorRepo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TwoFactorService {

    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final TwoFactorRepo twoFactorRepo;

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
                    return session;
                } else {
                    session.setAttempts(session.getAttempts() + 1);
                    twoFactorRepo.save(sessionId, session);
                }
            } else {
                throw new TwoFactorSessionExpiredException();
            }
        }
        return session;
    }

    public String addTwoFactor(TwoFactorSession twoFactor) {
        String sessionId = UUID.randomUUID().toString();
        System.out.println(twoFactor.getCode());
        twoFactor.setCode(encoder.encode(twoFactor.getCode()));
        twoFactorRepo.save(sessionId, twoFactor, Duration.ofMinutes(2));
        return sessionId;
    }

}
