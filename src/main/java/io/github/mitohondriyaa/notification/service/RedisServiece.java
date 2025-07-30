package io.github.mitohondriyaa.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisServiece {
    private final StringRedisTemplate stringRedisTemplate;
    @Value("${message-id.ttl.minutes}")
    private Integer messageIdTtlMinutes;

    public void setValue(String key) {
        stringRedisTemplate.opsForValue().set(key, "", Duration.ofMinutes(messageIdTtlMinutes));
    }
}