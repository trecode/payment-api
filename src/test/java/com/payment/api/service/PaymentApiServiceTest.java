package com.payment.api.service;

import com.payment.api.dto.PaymentInitiationRequest;
import com.payment.api.exception.AmountLimitExceededException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test class for PaymentApiService
 */
class PaymentApiServiceTest {

    private final PaymentApiService paymentApiService = new PaymentApiService();

    @Test
    void testValidatePaymentInitiationRequestWithPositiveAmountAndValidIbanDigitsSum() {
        var paymentInitiationRequest =
                PaymentInitiationRequest.builder()
                        .debtorIBAN("NL05ABNA1122334455")
                        .creditorIBAN("NL05ABNA2233445566")
                        .amount("1.23")
                        .endToEndId(UUID.randomUUID().toString())
                        .build();

        paymentApiService.validatePaymentInitiationRequest(paymentInitiationRequest);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "0"})
    void testValidatePaymentInitiationRequestWithZeroAmountAndInvalidIbanDigitsSum(String amount) {
        var paymentInitiationRequest =
                PaymentInitiationRequest.builder()
                        .debtorIBAN("NL00ABNA0000000000")
                        .creditorIBAN("NL05ABNA2233445566")
                        .amount(amount)
                        .endToEndId(UUID.randomUUID().toString())
                        .build();

        paymentApiService.validatePaymentInitiationRequest(paymentInitiationRequest);
    }

    @Test
    void testValidatePaymentInitiationRequestWithPositiveAmountAndInvalidIbanDigitsSum() {
        var paymentInitiationRequest =
                PaymentInitiationRequest.builder()
                        .debtorIBAN("NL00ABNA0000000000")
                        .creditorIBAN("NL05ABNA2233445566")
                        .amount("1.23")
                        .endToEndId(UUID.randomUUID().toString())
                        .build();

        var exception = assertThrows(
                AmountLimitExceededException.class,
                () -> paymentApiService.validatePaymentInitiationRequest(paymentInitiationRequest));

        assertThat(exception.getMessage()).isEqualTo("Amount limit exceeded for amount 1.23 and debtor IBAN NL00ABNA0000000000");
    }
}
