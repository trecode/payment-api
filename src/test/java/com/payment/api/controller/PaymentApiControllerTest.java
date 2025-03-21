package com.payment.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.api.dto.ErrorReasonCode;
import com.payment.api.dto.PaymentInitiationRequest;
import com.payment.api.dto.TransactionStatus;
import com.payment.api.exception.AmountLimitExceededException;
import com.payment.api.service.PaymentApiService;
import com.payment.api.util.Constants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for PaymentApiController
 */
@WebMvcTest
class PaymentApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PaymentApiService paymentApiService;

    @Test
    void testInitiatePaymentWithoutRequestIdHeader() throws Exception {
        mockMvc.perform(post(Constants.INITIATE_PAYMENT_URI))
                .andExpect(status().isBadRequest())
                .andExpect(header().doesNotExist(Constants.X_REQUEST_ID_HEADER))
                .andExpect(jsonPath("reason").value("Required request header 'X-Request-Id' for method parameter type UUID is not present"))
                .andExpect(jsonPath("reasonCode").value(ErrorReasonCode.INVALID_REQUEST.toString()))
                .andExpect(jsonPath("status").value(TransactionStatus.REJECTED.toString()));
    }

    @Test
    void testInitiatePaymentWithoutRequestBody() throws Exception {
        var requestId = UUID.randomUUID();

        mockMvc.perform(
                        post(Constants.INITIATE_PAYMENT_URI).header(Constants.X_REQUEST_ID_HEADER, requestId))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(Constants.X_REQUEST_ID_HEADER, requestId.toString()))
                .andExpect(jsonPath("reason").value("Invalid request body"))
                .andExpect(jsonPath("reasonCode").value(ErrorReasonCode.INVALID_REQUEST.toString()))
                .andExpect(jsonPath("status").value(TransactionStatus.REJECTED.toString()));
    }

    @Test
    void testInitiatePaymentWithInvalidRequestBody() throws Exception {
        var requestId = UUID.randomUUID();

        mockMvc.perform(
                        post(Constants.INITIATE_PAYMENT_URI).header(Constants.X_REQUEST_ID_HEADER, requestId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"debtorIBAN\":\"\",,\"creditorIBAN\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(Constants.X_REQUEST_ID_HEADER, requestId.toString()))
                .andExpect(jsonPath("reason").value("Invalid request body"))
                .andExpect(jsonPath("reasonCode").value(ErrorReasonCode.INVALID_REQUEST.toString()))
                .andExpect(jsonPath("status").value(TransactionStatus.REJECTED.toString()));
    }

    @Test
    void testInitiatePaymentWhenPaymentInitiationRequestHasNoData() throws Exception {
        var requestId = UUID.randomUUID();

        var paymentInitiationRequest =
                PaymentInitiationRequest.builder()
                        .build();

        var paymentInitiationRequestString = objectMapper.writeValueAsString(paymentInitiationRequest);

        mockMvc.perform(
                        post(Constants.INITIATE_PAYMENT_URI).header(Constants.X_REQUEST_ID_HEADER, requestId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(paymentInitiationRequestString))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(Constants.X_REQUEST_ID_HEADER, requestId.toString()))
                .andExpect(jsonPath(
                        "reason",
                        allOf(
                                containsString("debtorIBAN: must not be null"),
                                containsString("creditorIBAN: must not be null"),
                                containsString("amount: must not be null"),
                                containsString("endToEndId: must not be null"))))
                .andExpect(jsonPath("reasonCode").value(ErrorReasonCode.INVALID_REQUEST.toString()))
                .andExpect(jsonPath("status").value(TransactionStatus.REJECTED.toString()));
    }

    @Test
    void testInitiatePaymentWhenPaymentInitiationRequestHasInvalidData() throws Exception {
        var requestId = UUID.randomUUID();

        var paymentInitiationRequest =
                PaymentInitiationRequest.builder()
                        .debtorIBAN("NL")
                        .creditorIBAN("05")
                        .amount(".123")
                        .currency("E")
                        .build();

        var paymentInitiationRequestString = objectMapper.writeValueAsString(paymentInitiationRequest);

        mockMvc.perform(
                        post(Constants.INITIATE_PAYMENT_URI).header(Constants.X_REQUEST_ID_HEADER, requestId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(paymentInitiationRequestString))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(Constants.X_REQUEST_ID_HEADER, requestId.toString()))
                .andExpect(jsonPath(
                        "reason",
                        allOf(
                                containsString("debtorIBAN: must be in a valid format"),
                                containsString("creditorIBAN: must be in a valid format"),
                                containsString("amount: must be in a valid format"),
                                containsString("currency: must be in a valid format"),
                                containsString("endToEndId: must not be null"))))
                .andExpect(jsonPath("reasonCode").value(ErrorReasonCode.INVALID_REQUEST.toString()))
                .andExpect(jsonPath("status").value(TransactionStatus.REJECTED.toString()));
    }

    @Test
    void testInitiatePaymentWhenPaymentInitiationRequestHasValidData() throws Exception {
        var requestId = UUID.randomUUID();

        var paymentInitiationRequest =
                PaymentInitiationRequest.builder()
                        .debtorIBAN("NL05ABNA1122334455")
                        .creditorIBAN("NL05ABNA2233445566")
                        .amount("1.23")
                        .endToEndId(UUID.randomUUID().toString())
                        .build();

        var paymentInitiationRequestString = objectMapper.writeValueAsString(paymentInitiationRequest);

        mockMvc.perform(
                        post(Constants.INITIATE_PAYMENT_URI).header(Constants.X_REQUEST_ID_HEADER, requestId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(paymentInitiationRequestString))
                .andExpect(status().isCreated())
                .andExpect(header().string(Constants.X_REQUEST_ID_HEADER, requestId.toString()))
                .andExpect(jsonPath("paymentId").exists())
                .andExpect(jsonPath("status").value(TransactionStatus.ACCEPTED.toString()));
    }

    @Test
    void testInitiatePaymentWhenUnexpectedExceptionIsThrown() throws Exception {
        var requestId = UUID.randomUUID();

        var paymentInitiationRequest =
                PaymentInitiationRequest.builder()
                        .debtorIBAN("NL05ABNA1122334455")
                        .creditorIBAN("NL05ABNA2233445566")
                        .amount("1.23")
                        .endToEndId(UUID.randomUUID().toString())
                        .build();

        doThrow(new RuntimeException("Unexpected error"))
                .when(paymentApiService)
                .validatePaymentInitiationRequest(paymentInitiationRequest);

        var paymentInitiationRequestString = objectMapper.writeValueAsString(paymentInitiationRequest);

        mockMvc.perform(
                        post(Constants.INITIATE_PAYMENT_URI).header(Constants.X_REQUEST_ID_HEADER, requestId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(paymentInitiationRequestString))
                .andExpect(status().isInternalServerError())
                .andExpect(header().string(Constants.X_REQUEST_ID_HEADER, requestId.toString()))
                .andExpect(jsonPath("reason").value("Unexpected error"))
                .andExpect(jsonPath("reasonCode").value(ErrorReasonCode.GENERAL_ERROR.toString()))
                .andExpect(jsonPath("status").value(TransactionStatus.REJECTED.toString()));
    }

    @Test
    void testInitiatePaymentWhenAmountLimitExceededExceptionIsThrown() throws Exception {
        var requestId = UUID.randomUUID();

        var paymentInitiationRequest =
                PaymentInitiationRequest.builder()
                        .debtorIBAN("NL00ABNA0000000000")
                        .creditorIBAN("NL05ABNA2233445566")
                        .amount("1.23")
                        .endToEndId(UUID.randomUUID().toString())
                        .build();

        doThrow(new AmountLimitExceededException("Amount limit exceeded for amount 1.23 and debtor IBAN %NL05ABNA1122334455"))
                .when(paymentApiService)
                .validatePaymentInitiationRequest(paymentInitiationRequest);

        var paymentInitiationRequestString = objectMapper.writeValueAsString(paymentInitiationRequest);

        mockMvc.perform(
                        post(Constants.INITIATE_PAYMENT_URI).header(Constants.X_REQUEST_ID_HEADER, requestId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(paymentInitiationRequestString))
                .andExpect(status().is(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(header().string(Constants.X_REQUEST_ID_HEADER, requestId.toString()))
                .andExpect(jsonPath("reason").value("Amount limit exceeded for amount 1.23 and debtor IBAN %NL05ABNA1122334455"))
                .andExpect(jsonPath("reasonCode").value(ErrorReasonCode.LIMIT_EXCEEDED.toString()))
                .andExpect(jsonPath("status").value(TransactionStatus.REJECTED.toString()));
    }
}
