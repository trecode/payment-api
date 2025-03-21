package com.payment.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents payment initiation request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInitiationRequest {

    @NotNull
    @Pattern(regexp = "[A-Z]{2}[0-9]{2}[a-zA-Z0-9]{1,30}", message = "must be in a valid format")
    private String debtorIBAN;

    @NotNull
    @Pattern(regexp = "[A-Z]{2}[0-9]{2}[a-zA-Z0-9]{1,30}", message = "must be in a valid format")
    private String creditorIBAN;

    @NotNull
    @Pattern(regexp = "-?[0-9]+(\\.[0-9]{1,3})?", message = "must be in a valid format")
    private String amount;

    @Pattern(regexp = "[A-Z]{3}", message = "must be in a valid format")
    private String currency = "EUR";

    @NotNull
    private String endToEndId;
}
