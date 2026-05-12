package com.payments.saga;
import com.payments.domain.Payment;
import com.payments.exception.FraudDetectedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
@Service @Slf4j
public class FraudDetectionService {
    private static final BigDecimal HIGH_VALUE = new BigDecimal("50000.00");
    public void checkFraud(Payment p) {
        if (p.getPayerId().equals(p.getPayeeId()))
            throw new FraudDetectedException("Self-payment detected: " + p.getId());
        if (p.getAmount().compareTo(BigDecimal.ZERO) <= 0)
            throw new FraudDetectedException("Non-positive amount: " + p.getId());
        if (p.getAmount().compareTo(HIGH_VALUE) > 0)
            log.warn("High-value transaction flagged for review: {}", p.getId());
        log.debug("Fraud check passed for {}", p.getId());
    }
}
