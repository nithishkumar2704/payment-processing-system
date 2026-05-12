package com.payments.saga;
import com.payments.domain.Payment;
import com.payments.exception.InsufficientFundsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;
@Service @Slf4j
public class AccountService {
    private final ConcurrentHashMap<String, BigDecimal> balances = new ConcurrentHashMap<>() {{
        put("payer-001", new BigDecimal("10000.00"));
        put("payer-002", new BigDecimal("5000.00"));
        put("payee-001", BigDecimal.ZERO);
        put("payee-002", BigDecimal.ZERO);
    }};
    private final ConcurrentHashMap<String, BigDecimal> holds = new ConcurrentHashMap<>();

    public void reserveFunds(Payment p) {
        BigDecimal bal = balances.getOrDefault(p.getPayerId(), BigDecimal.ZERO);
        if (bal.compareTo(p.getAmount()) < 0)
            throw new InsufficientFundsException("Insufficient funds. Balance=" + bal + " required=" + p.getAmount());
        balances.merge(p.getPayerId(), p.getAmount(), BigDecimal::subtract);
        holds.put(p.getId(), p.getAmount());
        log.info("Reserved {} {} for {}", p.getAmount(), p.getCurrency(), p.getId());
    }
    public void transferFunds(Payment p) {
        BigDecimal held = holds.remove(p.getId());
        if (held == null) throw new IllegalStateException("No hold for payment " + p.getId());
        balances.merge(p.getPayeeId(), held, BigDecimal::add);
        log.info("Transferred {} to {} for {}", held, p.getPayeeId(), p.getId());
    }
    public void releaseFunds(Payment p) {
        BigDecimal held = holds.remove(p.getId());
        if (held != null) { balances.merge(p.getPayerId(), held, BigDecimal::add); log.info("Released {} for {}", held, p.getId()); }
    }
    public void reverseTransfer(Payment p) {
        balances.merge(p.getPayeeId(), p.getAmount(), BigDecimal::subtract);
        balances.merge(p.getPayerId(), p.getAmount(), BigDecimal::add);
        log.info("Reversed transfer of {} for {}", p.getAmount(), p.getId());
    }
    public BigDecimal getBalance(String accountId) { return balances.getOrDefault(accountId, BigDecimal.ZERO); }
}
