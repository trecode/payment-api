package com.payment.api.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * Represents accepted payment response
 */
@Data
@Builder
public class PaymentAcceptedResponse {

    private UUID paymentId;

    @Builder.Default
    private TransactionStatus status = TransactionStatus.ACCEPTED;
}
