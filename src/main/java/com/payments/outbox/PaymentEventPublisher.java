package com.payments.outbox;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.payments.domain.OutboxEvent;
import com.payments.domain.Payment;
import com.payments.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
@Service @RequiredArgsConstructor @Slf4j
public class PaymentEventPublisher {
    private final OutboxEventRepository outboxRepo;
    private final ObjectMapper objectMapper;
    public void publishPaymentInitiated(Payment p)  { save(p, "PAYMENT_INITIATED",  "payments.payment_initiated"); }
    public void publishPaymentCompleted(Payment p)  { save(p, "PAYMENT_COMPLETED",  "payments.payment_completed"); }
    public void publishPaymentFailed(Payment p)     { save(p, "PAYMENT_FAILED",     "payments.payment_failed");    }
    private void save(Payment p, String type, String topic) {
        try {
            outboxRepo.save(OutboxEvent.builder()
                .aggregateId(p.getId()).eventType(type)
                .payload(objectMapper.writeValueAsString(p))
                .topic(topic).published(false).build());
        } catch (Exception e) { throw new RuntimeException("Outbox write failed", e); }
    }
}
