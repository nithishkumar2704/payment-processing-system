package com.payments.outbox;
import com.payments.domain.OutboxEvent;
import com.payments.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Component @Profile("!prod") @RequiredArgsConstructor @Slf4j
public class OutboxRelay {
    private final OutboxEventRepository outboxRepo;
    private final KafkaTemplate<String, String> kafkaTemplate;
    @Scheduled(fixedDelay = 200)
    @Transactional
    public void relay() {
        List<OutboxEvent> pending = outboxRepo.findTop100ByPublishedFalseOrderByCreatedAtAsc();
        if (pending.isEmpty()) return;
        for (OutboxEvent e : pending) {
            try {
                kafkaTemplate.send(e.getTopic(), e.getAggregateId(), e.getPayload());
                e.setPublished(true);
            } catch (Exception ex) { log.error("Relay failed for {}: {}", e.getId(), ex.getMessage()); }
        }
        outboxRepo.saveAll(pending);
    }
}
