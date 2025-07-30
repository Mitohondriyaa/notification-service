package io.github.mitohondriyaa.notification.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class RedisConfig {
    @Bean
    public RedisConnectionFactory redisCounterConnectionFactory() {
        return new LettuceConnectionFactory("localhost", 6380);
    }

    @Bean
    public StringRedisTemplate redisCounterStringRedisTemplate(
        RedisConnectionFactory redisCounterConnectionFactory
    ) {
        return new StringRedisTemplate(redisCounterConnectionFactory);
    }
}