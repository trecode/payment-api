package com.payment.api.controller;

import com.payment.api.dto.PaymentAcceptedResponse;
import com.payment.api.dto.PaymentInitiationRequest;
import com.payment.api.service.PaymentApiService;
import com.payment.api.util.Constants;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST controller for payment operations
 */
@RestController
@Slf4j
public class PaymentApiController {

    @Autowired
    private PaymentApiService paymentApiService;

    /**
     * Initiates a payment.
     *
     * @param requestId UUID provided by the client to identify the request
     * @param paymentInitiationRequest the payment initiation request body
     * @return response entity with payment id
     */
    @PostMapping(Constants.INITIATE_PAYMENT_URI)
    public ResponseEntity<PaymentAcceptedResponse> initiatePayment(
            @RequestHeader(Constants.X_REQUEST_ID_HEADER) UUID requestId,
            @RequestBody @Valid PaymentInitiationRequest paymentInitiationRequest) {
        log.debug("Initiating payment with request-id={} and request body={}", requestId, paymentInitiationRequest);

        paymentApiService.validatePaymentInitiationRequest(paymentInitiationRequest);
        var paymentId = UUID.randomUUID();

        log.debug("Initiated payment with request-id={} and payment-id={}", requestId, paymentId);

        var paymentAcceptedResponse =
                PaymentAcceptedResponse.builder()
                        .paymentId(paymentId)
                        .build();

        return ResponseEntity.status(HttpStatus.CREATED)
                .header(Constants.X_REQUEST_ID_HEADER, requestId.toString())
                .body(paymentAcceptedResponse);
    }
}
