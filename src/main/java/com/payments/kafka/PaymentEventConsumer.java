package com.payments.kafka;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.payments.domain.ProcessedEvent;
import com.payments.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
@Component @Profile("!prod") @RequiredArgsConstructor @Slf4j
public class PaymentEventConsumer {
    private final ProcessedEventRepository processedEventRepo;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "payments.payment_initiated", groupId = "payment-processor-group")
    @Transactional
    public void handleInitiated(@Payload String payload, @Header(KafkaHeaders.RECEIVED_KEY) String paymentId, Acknowledgment ack) {
        if (isDupe(paymentId + ":INIT")) { ack.acknowledge(); return; }
        try { log.info("PAYMENT_INITIATED consumed: {}", paymentId); mark(paymentId + ":INIT", "PAYMENT_INITIATED"); ack.acknowledge(); }
        catch (Exception e) { log.error("Failed PAYMENT_INITIATED {}: {}", paymentId, e.getMessage()); }
    }

    @KafkaListener(topics = "payments.payment_completed", groupId = "notification-group")
    @Transactional
    public void handleCompleted(@Payload String payload, @Header(KafkaHeaders.RECEIVED_KEY) String paymentId, Acknowledgment ack) {
        if (isDupe(paymentId + ":DONE")) { ack.acknowledge(); return; }
        try { log.info("PAYMENT_COMPLETED consumed: {}", paymentId); mark(paymentId + ":DONE", "PAYMENT_COMPLETED"); ack.acknowledge(); }
        catch (Exception e) { log.error("Failed PAYMENT_COMPLETED {}: {}", paymentId, e.getMessage()); }
    }

    @KafkaListener(topics = "payments.payment_failed", groupId = "notification-group")
    @Transactional
    public void handleFailed(@Payload String payload, @Header(KafkaHeaders.RECEIVED_KEY) String paymentId, Acknowledgment ack) {
        if (isDupe(paymentId + ":FAIL")) { ack.acknowledge(); return; }
        try { log.info("PAYMENT_FAILED consumed: {}", paymentId); mark(paymentId + ":FAIL", "PAYMENT_FAILED"); ack.acknowledge(); }
        catch (Exception e) { log.error("Failed PAYMENT_FAILED {}: {}", paymentId, e.getMessage()); }
    }

    private boolean isDupe(String id) { return processedEventRepo.existsById(id); }
    private void mark(String id, String type) {
        processedEventRepo.save(ProcessedEvent.builder().eventId(id).eventType(type).build());
    }
}
