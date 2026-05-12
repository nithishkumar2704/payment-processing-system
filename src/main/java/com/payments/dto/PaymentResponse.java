package com.payments.dto;
import com.payments.domain.Payment;
import com.payments.domain.PaymentStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PaymentResponse {
    private String id;
    private String idempotencyKey;
    private PaymentStatus status;
    private BigDecimal amount;
    private String currency;
    private String payerId;
    private String payeeId;
    private String description;
    private Instant createdAt;
    private Instant completedAt;
    private String failureReason;
    private String message;

    public static PaymentResponse from(Payment p) {
        return PaymentResponse.builder()
            .id(p.getId()).idempotencyKey(p.getIdempotencyKey())
            .status(p.getStatus()).amount(p.getAmount()).currency(p.getCurrency())
            .payerId(p.getPayerId()).payeeId(p.getPayeeId()).description(p.getDescription())
            .createdAt(p.getCreatedAt()).completedAt(p.getCompletedAt())
            .failureReason(p.getFailureReason()).build();
    }
    public static PaymentResponse queued(String key) {
        return PaymentResponse.builder().idempotencyKey(key)
            .status(PaymentStatus.QUEUED)
            .message("Payment queued — service temporarily degraded. Retry soon.").build();
    }
}
