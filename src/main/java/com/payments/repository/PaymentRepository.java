package com.payments.repository;
import com.payments.domain.Payment;
import com.payments.domain.PaymentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    Optional<Payment> findByIdempotencyKey(String key);
    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT p FROM Payment p WHERE p.id = :id")
    Optional<Payment> findByIdWithLock(@Param("id") String id);
    List<Payment> findByPayerIdAndStatusOrderByCreatedAtDesc(String payerId, PaymentStatus status, Pageable pageable);
    @Query("SELECT p FROM Payment p WHERE p.createdAt > :cursor ORDER BY p.createdAt ASC")
    List<Payment> findAfterCursor(@Param("cursor") Instant cursor, Pageable pageable);
    long countByStatus(PaymentStatus status);
}
