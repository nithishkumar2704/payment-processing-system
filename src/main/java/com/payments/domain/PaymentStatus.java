package com.payments.domain;
public enum PaymentStatus {
    PENDING, PROCESSING, COMPLETED, FAILED, REFUNDED, QUEUED;
    public boolean canTransitionTo(PaymentStatus next) {
        return switch (this) {
            case PENDING    -> next == PROCESSING || next == FAILED;
            case PROCESSING -> next == COMPLETED  || next == FAILED;
            case COMPLETED  -> next == REFUNDED;
            case QUEUED     -> next == PENDING;
            default         -> false;
        };
    }
}
