package com.payment.api.exception;

/**
 * Thrown to indicate that amount limit is exceeded
 */
public class AmountLimitExceededException extends RuntimeException {

    public AmountLimitExceededException(String message) {
        super(message);
    }
}
