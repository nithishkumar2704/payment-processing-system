package com.payments.domain;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;
@Entity
@Table(name = "processed_events")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProcessedEvent {
    @Id private String eventId;
    private String eventType;
    @CreationTimestamp private Instant processedAt;
}
