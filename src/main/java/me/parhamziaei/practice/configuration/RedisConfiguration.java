package me.parhamziaei.practice.configuration;

import me.parhamziaei.practice.entity.redis.EmailVerifySession;
import me.parhamziaei.practice.entity.redis.ForgotPasswordSession;
import me.parhamziaei.practice.entity.redis.TwoFactorSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

@Configuration
public class RedisConfiguration {

    @Bean
    public LettuceConnectionFactory lettuceConnectionFactory() {
        return new LettuceConnectionFactory();
    }

    @Bean
    public RedisTemplate<String, TwoFactorSession> twoFactorRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, TwoFactorSession> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(TwoFactorSession.class));
        return redisTemplate;
    }

    @Bean
    public RedisTemplate<String, EmailVerifySession> emailVerifyRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, EmailVerifySession> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(EmailVerifySession.class));
        return redisTemplate;
    }

    @Bean
    public RedisTemplate<String, ForgotPasswordSession> forgotPasswordRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, ForgotPasswordSession> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(ForgotPasswordSession.class));
        return redisTemplate;
    }

}
