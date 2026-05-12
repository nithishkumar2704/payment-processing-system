package com.payments.exception;
import com.payments.domain.PaymentStatus;
public class InvalidStateTransitionException extends RuntimeException {
    public InvalidStateTransitionException(PaymentStatus from, PaymentStatus to) {
        super("Cannot transition from " + from + " to " + to);
    }
}
