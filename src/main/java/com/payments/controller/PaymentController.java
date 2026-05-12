package com.payments.controller;
import com.payments.dto.PaymentRequest;
import com.payments.dto.PaymentResponse;
import com.payments.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor @Slf4j
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest request) {
        log.info("POST /payments idempotencyKey={} amount={}", request.getIdempotencyKey(), request.getAmount());
        PaymentResponse response = paymentService.processPayment(request);
        HttpStatus status = switch (response.getStatus()) {
            case COMPLETED -> HttpStatus.CREATED;
            case QUEUED    -> HttpStatus.ACCEPTED;
            default        -> HttpStatus.OK;
        };
        return ResponseEntity.status(status).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable String id) {
        return ResponseEntity.ok(paymentService.getPayment(id));
    }

    @GetMapping("/payer/{payerId}")
    public ResponseEntity<List<PaymentResponse>> getByPayer(
            @PathVariable String payerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(paymentService.getPaymentsByPayer(payerId, page, size));
    }

    @GetMapping("/stats")
    public ResponseEntity<PaymentService.PaymentStats> stats() {
        return ResponseEntity.ok(paymentService.getStats());
    }
}
