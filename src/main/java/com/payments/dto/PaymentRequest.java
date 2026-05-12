package com.payments.dto;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PaymentRequest {
    @NotBlank(message = "Idempotency key is required")
    private String idempotencyKey;
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be > 0")
    @Digits(integer = 15, fraction = 4)
    private BigDecimal amount;
    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code")
    private String currency;
    @NotBlank(message = "Payer ID is required")
    private String payerId;
    @NotBlank(message = "Payee ID is required")
    private String payeeId;
    private String description;
}
