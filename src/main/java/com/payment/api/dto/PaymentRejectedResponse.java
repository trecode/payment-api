package com.payment.api.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Represents rejected payment response
 */
@Data
@Builder
public class PaymentRejectedResponse {

    private String reason;
    private ErrorReasonCode reasonCode;

    @Builder.Default
    private TransactionStatus status = TransactionStatus.REJECTED;
}
