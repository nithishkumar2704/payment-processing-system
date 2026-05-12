package com.payments.config;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.*;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.*;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
@Configuration @Profile("local") @Slf4j
public class LocalConfig {
    /** No-op RedisTemplate — all operations succeed silently */
    @Bean
    public RedisTemplate<String, String> redisTemplate() {
        log.info("[LOCAL] No-op RedisTemplate active");
        return new RedisTemplate<>() {
            @Override public void afterPropertiesSet() {}
        };
    }
    /** No-op KafkaTemplate — messages are silently discarded */
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        log.info("[LOCAL] No-op KafkaTemplate active");
        var factory = new DefaultKafkaProducerFactory<String,String>(Map.of(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092",
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,   StringSerializer.class,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class
        ));
        return new KafkaTemplate<>(factory) {
            @Override public CompletableFuture<org.springframework.kafka.support.SendResult<String,String>>
            send(String topic, String key, String data) {
                log.debug("[LOCAL] Kafka discarded: topic={} key={}", topic, key);
                return CompletableFuture.completedFuture(null);
            }
        };
    }
}
