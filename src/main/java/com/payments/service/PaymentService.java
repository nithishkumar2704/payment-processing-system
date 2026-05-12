package com.payments.service;
import com.payments.domain.Payment;
import com.payments.domain.PaymentStatus;
import com.payments.dto.PaymentRequest;
import com.payments.dto.PaymentResponse;
import com.payments.exception.InvalidStateTransitionException;
import com.payments.exception.PaymentNotFoundException;
import com.payments.outbox.PaymentEventPublisher;
import com.payments.repository.PaymentRepository;
import com.payments.saga.SagaOrchestrator;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
@Service @RequiredArgsConstructor @Slf4j
public class PaymentService {
    private final PaymentRepository paymentRepo;
    private final IdempotencyService idempotencyService;
    private final PaymentEventPublisher eventPublisher;
    private final SagaOrchestrator sagaOrchestrator;

    @CircuitBreaker(name = "paymentService", fallbackMethod = "processPaymentFallback")
    @Retry(name = "paymentService")
    @Bulkhead(name = "paymentService")
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        Optional<PaymentResponse> cached = idempotencyService.getOrLock(request.getIdempotencyKey(), PaymentResponse.class);
        if (cached.isPresent()) return cached.get();
        try {
            Payment payment = Payment.builder()
                .idempotencyKey(request.getIdempotencyKey())
                .status(PaymentStatus.PENDING)
                .amount(request.getAmount()).currency(request.getCurrency())
                .payerId(request.getPayerId()).payeeId(request.getPayeeId())
                .description(request.getDescription()).build();
            paymentRepo.save(payment);
            eventPublisher.publishPaymentInitiated(payment);
            sagaOrchestrator.startPaymentSaga(payment.getId());
            Payment final2 = paymentRepo.findById(payment.getId()).orElse(payment);
            PaymentResponse resp = PaymentResponse.from(final2);
            idempotencyService.saveResponse(request.getIdempotencyKey(), resp);
            return resp;
        } catch (Exception ex) {
            idempotencyService.clearLock(request.getIdempotencyKey());
            log.error("Payment failed for key {}: {}", request.getIdempotencyKey(), ex.getMessage());
            throw ex;
        }
    }

    public PaymentResponse processPaymentFallback(PaymentRequest request, Exception ex) {
        log.warn("Circuit OPEN — queuing {}: {}", request.getIdempotencyKey(), ex.getClass().getSimpleName());
        return PaymentResponse.queued(request.getIdempotencyKey());
    }

    @Transactional
    public Payment updateStatus(String paymentId, PaymentStatus newStatus) {
        Payment p = paymentRepo.findByIdWithLock(paymentId).orElseThrow(() -> new PaymentNotFoundException(paymentId));
        if (!p.getStatus().canTransitionTo(newStatus)) throw new InvalidStateTransitionException(p.getStatus(), newStatus);
        p.setStatus(newStatus);
        if (newStatus == PaymentStatus.COMPLETED) p.setCompletedAt(Instant.now());
        return paymentRepo.save(p);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPayment(String id) {
        return PaymentResponse.from(paymentRepo.findById(id).orElseThrow(() -> new PaymentNotFoundException(id)));
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByPayer(String payerId, int page, int size) {
        return paymentRepo.findByPayerIdAndStatusOrderByCreatedAtDesc(payerId, PaymentStatus.COMPLETED, PageRequest.of(page, size))
            .stream().map(PaymentResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public PaymentStats getStats() {
        return new PaymentStats(
            paymentRepo.countByStatus(PaymentStatus.PENDING),
            paymentRepo.countByStatus(PaymentStatus.PROCESSING),
            paymentRepo.countByStatus(PaymentStatus.COMPLETED),
            paymentRepo.countByStatus(PaymentStatus.FAILED));
    }
    public record PaymentStats(long pending, long processing, long completed, long failed) {}
}
