package com.payments.domain;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.Instant;
@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_idempotency_key", columnList = "idempotencyKey", unique = true),
    @Index(name = "idx_payment_status",  columnList = "status"),
    @Index(name = "idx_payment_payer",   columnList = "payerId"),
    @Index(name = "idx_payment_created", columnList = "createdAt")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(unique = true, nullable = false)
    private String idempotencyKey;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private PaymentStatus status;
    @Column(precision = 19, scale = 4, nullable = false)
    private BigDecimal amount;
    @Column(nullable = false, length = 3)
    private String currency;
    @Column(nullable = false) private String payerId;
    @Column(nullable = false) private String payeeId;
    private String description;
    @Version private Long version;
    @CreationTimestamp private Instant createdAt;
    @UpdateTimestamp  private Instant updatedAt;
    private Instant completedAt;
    private String failureReason;
}
