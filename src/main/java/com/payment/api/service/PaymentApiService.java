package com.payment.api.service;

import com.payment.api.dto.PaymentInitiationRequest;
import com.payment.api.exception.AmountLimitExceededException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Service that validates payment initiation requests
 */
@Service
@Slf4j
public class PaymentApiService {

    /**
     * Validates a payment initiation request.
     *
     * @param paymentInitiationRequest payment initiation request
     */
    public void validatePaymentInitiationRequest(PaymentInitiationRequest paymentInitiationRequest) {
        var amount = new BigDecimal(paymentInitiationRequest.getAmount());
        var debtorIban = paymentInitiationRequest.getDebtorIBAN();

        // Calculates the sum of all digits of the debtor IBAN
        var debtorIbanDigitsSum =
                debtorIban
                        .chars()
                        .filter(Character::isDigit)
                        .map(Character::getNumericValue)
                        .sum();

        // IBAN digits sum is considered invalid if "sum mod IBAN length" operation result is equal to zero
        var isIbanDigitsSumInvalid = debtorIbanDigitsSum % debtorIban.length() == 0;

        log.debug("Validating limit for amount={} and IBAN={}", amount, debtorIban);

        if (amount.compareTo(BigDecimal.ZERO) > 0 && isIbanDigitsSumInvalid) {
            throw new AmountLimitExceededException(
                    "Amount limit exceeded for amount %s and debtor IBAN %s".formatted(amount, debtorIban));
        }
    }
}
