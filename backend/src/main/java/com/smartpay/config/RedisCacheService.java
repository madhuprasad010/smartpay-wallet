package com.smartpay.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisCacheService {

    private static final Logger log = LoggerFactory.getLogger(RedisCacheService.class);

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public <T> Optional<T> get(String key, Class<T> clazz) {
        try {
            Object val = redisTemplate.opsForValue().get(key);
            if (val == null) {
                return Optional.empty();
            }
            if (clazz.isInstance(val)) {
                return Optional.of(clazz.cast(val));
            }
            String json = objectMapper.writeValueAsString(val);
            return Optional.of(objectMapper.readValue(json, clazz));
        } catch (Exception e) {
            log.debug("Redis cache get error for key {}: {}. Falling back to DB.", key, e.getMessage());
            return Optional.empty();
        }
    }

    public void set(String key, Object value, long ttlSeconds) {
        try {
            redisTemplate.opsForValue().set(key, value, ttlSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.debug("Redis cache set error for key {}: {}. Proceeding without cache.", key, e.getMessage());
        }
    }

    public void delete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.debug("Redis cache delete error for key {}: {}", key, e.getMessage());
        }
    }

    public boolean isRateLimited(String key, int maxRequests, long windowSeconds) {
        try {
            Long count = redisTemplate.opsForValue().increment(key);
            if (count != null && count == 1) {
                redisTemplate.expire(key, windowSeconds, TimeUnit.SECONDS);
            }
            return count != null && count > maxRequests;
        } catch (Exception e) {
            log.debug("Redis rate limiting error for key {}: {}. Allowing request.", key, e.getMessage());
            return false;
        }
    }
}
