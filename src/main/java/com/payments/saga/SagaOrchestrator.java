package com.payments.saga;
import com.payments.domain.Payment;
import com.payments.domain.PaymentStatus;
import com.payments.outbox.PaymentEventPublisher;
import com.payments.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;
@Component @RequiredArgsConstructor @Slf4j
public class SagaOrchestrator {
    private final PaymentRepository paymentRepo;
    private final PaymentEventPublisher eventPublisher;
    private final AccountService accountService;
    private final FraudDetectionService fraudService;

    @Transactional
    public void startPaymentSaga(String paymentId) {
        Payment payment = paymentRepo.findById(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));
        List<String> done = new ArrayList<>();
        try {
            fraudService.checkFraud(payment);   done.add("FRAUD_CHECK");
            accountService.reserveFunds(payment); done.add("FUNDS_RESERVED");
            accountService.transferFunds(payment); done.add("FUNDS_TRANSFERRED");
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setCompletedAt(Instant.now());
            paymentRepo.save(payment);
            eventPublisher.publishPaymentCompleted(payment);
            log.info("Saga COMPLETED for {}", paymentId);
        } catch (Exception ex) {
            log.error("Saga failed after {}: {}", done, ex.getMessage());
            compensate(done, payment, ex.getMessage());
        }
    }

    private void compensate(List<String> done, Payment payment, String reason) {
        ListIterator<String> it = done.listIterator(done.size());
        while (it.hasPrevious()) {
            String step = it.previous();
            try {
                switch (step) {
                    case "FUNDS_TRANSFERRED" -> accountService.reverseTransfer(payment);
                    case "FUNDS_RESERVED"    -> accountService.releaseFunds(payment);
                    default -> {}
                }
            } catch (Exception e) { log.error("Compensation failed for {}: {}", step, e.getMessage()); }
        }
        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailureReason(reason);
        paymentRepo.save(payment);
        eventPublisher.publishPaymentFailed(payment);
    }
}
