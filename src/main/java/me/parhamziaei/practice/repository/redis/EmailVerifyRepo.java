package me.parhamziaei.practice.repository.redis;

import lombok.RequiredArgsConstructor;
import me.parhamziaei.practice.entity.redis.EmailVerifySession;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class EmailVerifyRepo {

    private final RedisTemplate<String, EmailVerifySession> redisTemplate;

    public String getKey(String sessionId) {
        return "register:verify-session:" + sessionId;
    }

    public void save(String sessionId, EmailVerifySession session, Duration ttl) {
        redisTemplate.opsForValue().set(getKey(sessionId), session, ttl);
    }

    public void save(String sessionId, EmailVerifySession session) {
        redisTemplate.opsForValue().set(getKey(sessionId), session);
    }

    public EmailVerifySession get(String sessionId) {
        return redisTemplate.opsForValue().get(getKey(sessionId));
    }

    public void remove(String sessionId) {
        redisTemplate.delete(getKey(sessionId));
    }

}
