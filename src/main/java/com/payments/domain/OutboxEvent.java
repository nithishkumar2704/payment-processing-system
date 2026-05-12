package com.payments.domain;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;
@Entity
@Table(name = "payment_outbox", indexes = {
    @Index(name = "idx_outbox_unpublished", columnList = "published,createdAt")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OutboxEvent {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(nullable = false) private String aggregateId;
    @Column(nullable = false) private String eventType;
    @Column(columnDefinition = "TEXT", nullable = false) private String payload;
    @Column(nullable = false) private boolean published;
    private String topic;
    @CreationTimestamp private Instant createdAt;
}
