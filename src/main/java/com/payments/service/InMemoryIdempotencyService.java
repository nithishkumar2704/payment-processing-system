package com.payments.service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.payments.exception.PaymentConflictException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
@Service @Primary
@ConditionalOnProperty(name = "payment.idempotency.backend", havingValue = "memory")
@Slf4j
public class InMemoryIdempotencyService extends IdempotencyService {
    private final ConcurrentHashMap<String, String> store = new ConcurrentHashMap<>();
    private final ObjectMapper mapper;
    public InMemoryIdempotencyService(ObjectMapper objectMapper) {
        super(null, objectMapper); this.mapper = objectMapper;
    }
    @Override public <T> Optional<T> getOrLock(String key, Class<T> type) {
        String prev = store.putIfAbsent(key, "PROCESSING");
        if (prev == null) return Optional.empty();
        if ("PROCESSING".equals(prev)) throw new PaymentConflictException("Concurrent request: " + key);
        try { return Optional.of(mapper.readValue(prev, type)); }
        catch (Exception e) { store.remove(key); return Optional.empty(); }
    }
    @Override public <T> void saveResponse(String key, T response) {
        try { store.put(key, mapper.writeValueAsString(response)); }
        catch (Exception e) { log.error("InMemory save failed for {}", key); }
    }
    @Override public void clearLock(String key) { store.remove(key, "PROCESSING"); }
}
