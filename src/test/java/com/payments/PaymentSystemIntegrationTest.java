package com.payments;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.payments.domain.PaymentStatus;
import com.payments.dto.PaymentRequest;
import com.payments.dto.PaymentResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import java.math.BigDecimal;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("local")
class PaymentSystemIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test void shouldProcessPayment() throws Exception {
        PaymentRequest req = PaymentRequest.builder()
            .idempotencyKey(UUID.randomUUID().toString()).amount(new BigDecimal("100.00"))
            .currency("USD").payerId("payer-001").payeeId("payee-001").description("Test").build();
        MvcResult r = mockMvc.perform(post("/api/v1/payments")
            .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req)))
            .andExpect(status().is2xxSuccessful()).andReturn();
        PaymentResponse resp = objectMapper.readValue(r.getResponse().getContentAsString(), PaymentResponse.class);
        assertThat(resp.getId()).isNotNull();
        assertThat(resp.getStatus()).isIn(PaymentStatus.COMPLETED, PaymentStatus.FAILED);
    }

    @Test void shouldReturnSameResponseForDuplicateKey() throws Exception {
        String key = UUID.randomUUID().toString();
        PaymentRequest req = PaymentRequest.builder()
            .idempotencyKey(key).amount(new BigDecimal("50.00"))
            .currency("USD").payerId("payer-001").payeeId("payee-001").build();
        String body = objectMapper.writeValueAsString(req);
        MvcResult r1 = mockMvc.perform(post("/api/v1/payments").contentType(MediaType.APPLICATION_JSON).content(body)).andReturn();
        MvcResult r2 = mockMvc.perform(post("/api/v1/payments").contentType(MediaType.APPLICATION_JSON).content(body)).andReturn();
        PaymentResponse p1 = objectMapper.readValue(r1.getResponse().getContentAsString(), PaymentResponse.class);
        PaymentResponse p2 = objectMapper.readValue(r2.getResponse().getContentAsString(), PaymentResponse.class);
        assertThat(p1.getId()).isEqualTo(p2.getId());
    }

    @Test void shouldRejectInvalidRequest() throws Exception {
        PaymentRequest bad = PaymentRequest.builder().idempotencyKey("").amount(new BigDecimal("-1")).currency("USDX").build();
        mockMvc.perform(post("/api/v1/payments").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(bad)))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test void shouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/payments/nonexistent")).andExpect(status().isNotFound());
    }

    @Test void shouldReturnStats() throws Exception {
        mockMvc.perform(get("/api/v1/payments/stats")).andExpect(status().isOk()).andExpect(jsonPath("$.completed").isNumber());
    }
}
