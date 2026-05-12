package com.payments.service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.payments.exception.PaymentConflictException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.Optional;
@Service @Slf4j
public class IdempotencyService {
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private static final Duration TTL = Duration.ofHours(24);
    private static final String PREFIX = "idempotency:";
    private static final String PROCESSING = "PROCESSING";
    IdempotencyService(RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate; this.objectMapper = objectMapper;
    }
    public <T> Optional<T> getOrLock(String key, Class<T> type) {
        String rk = PREFIX + key;
        String cached = redisTemplate.opsForValue().get(rk);
        if (cached != null) {
            if (PROCESSING.equals(cached)) throw new PaymentConflictException("Still processing: " + key);
            try { return Optional.of(objectMapper.readValue(cached, type)); }
            catch (Exception e) { log.warn("Deserialize failed for {}", key); }
        }
        Boolean ok = redisTemplate.opsForValue().setIfAbsent(rk, PROCESSING, TTL);
        if (Boolean.FALSE.equals(ok)) throw new PaymentConflictException("Concurrent request: " + key);
        return Optional.empty();
    }
    public <T> void saveResponse(String key, T response) {
        try { redisTemplate.opsForValue().set(PREFIX + key, objectMapper.writeValueAsString(response), TTL); }
        catch (Exception e) { log.error("Save idempotency failed for {}", key); }
    }
    public void clearLock(String key) {
        String rk = PREFIX + key;
        if (PROCESSING.equals(redisTemplate.opsForValue().get(rk))) redisTemplate.delete(rk);
    }
}
