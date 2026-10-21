package com.example.demo.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisFlushRunner implements CommandLineRunner {

    private final StringRedisTemplate redisTemplate;

    public RedisFlushRunner(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void run(String... args) {
        if (redisTemplate == null || redisTemplate.getConnectionFactory() == null) {
            return;
        }

        try (var connection = redisTemplate.getConnectionFactory().getConnection()) {
            connection.serverCommands().flushAll();
            System.out.println("=== RENDER REDIS FLUSHED SUCCESSFULLY ===");
        } catch (Exception e) {
            System.out.println("=== Redis unavailable, skipping flush on startup ===");
        }
    }
}