package me.parhamziaei.practice.repository.redis;

import lombok.RequiredArgsConstructor;
import me.parhamziaei.practice.entity.redis.TwoFactorSession;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class TwoFactorRepo {

    private final RedisTemplate<String, TwoFactorSession> redisTemplate;

    public String getKey(String sessionId) {
        return "login:2fa:" + sessionId;
    }

    public void save(String sessionId, TwoFactorSession twoFactor, Duration ttl) {
        redisTemplate.opsForValue().set(getKey(sessionId), twoFactor, ttl);
    }

    public void save(String sessionId, TwoFactorSession twoFactor) {
        redisTemplate.opsForValue().set(getKey(sessionId), twoFactor);
    }

    public TwoFactorSession get(String sessionId) {
        return redisTemplate.opsForValue().get(getKey(sessionId));
    }

    public void remove(String sessionId) {
        redisTemplate.delete(getKey(sessionId));
    }

}
