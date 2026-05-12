package com.payments.exception;
public class PaymentConflictException extends RuntimeException {
    public PaymentConflictException(String msg) { super(msg); }
}
