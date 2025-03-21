package com.payment.api.exception;

import com.payment.api.dto.ErrorReasonCode;
import com.payment.api.dto.PaymentRejectedResponse;
import com.payment.api.util.Constants;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.stream.Collectors;

/**
 * Handles payment API exceptions
 */
@ControllerAdvice
@Slf4j
public class PaymentApiExceptionHandler {

    /**
     * Handles MethodArgumentNotValidException exception.
     *
     * @param exception handled exception
     * @param request incoming request
     * @return response entity with error information and 400 status code
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<PaymentRejectedResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        var requestId = request.getHeader(Constants.X_REQUEST_ID_HEADER);

        var reason = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> "%s: %s".formatted(error.getField(), error.getDefaultMessage()))
                .collect(Collectors.joining(", "));

        log.debug("Request-id={}, reason={}", requestId, reason, exception);

        var paymentRejectedResponse =
                PaymentRejectedResponse.builder()
                        .reason(reason)
                        .reasonCode(ErrorReasonCode.INVALID_REQUEST)
                        .build();

        return ResponseEntity.badRequest()
                .header(Constants.X_REQUEST_ID_HEADER, requestId)
                .body(paymentRejectedResponse);
    }

    /**
     * Handles HttpMessageNotReadableException exception.
     *
     * @param exception handled exception
     * @param request incoming request
     * @return response entity with error information and 400 status code
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<PaymentRejectedResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException exception,
            HttpServletRequest request) {
        var requestId = request.getHeader(Constants.X_REQUEST_ID_HEADER);
        var reason = "Invalid request body";

        log.debug("Request-id={}, reason={}", requestId, reason, exception);

        var paymentRejectedResponse =
                PaymentRejectedResponse.builder()
                        .reason("Invalid request body")
                        .reasonCode(ErrorReasonCode.INVALID_REQUEST)
                        .build();

        return ResponseEntity.badRequest()
                .header(Constants.X_REQUEST_ID_HEADER, requestId)
                .body(paymentRejectedResponse);
    }

    /**
     * Handles MissingRequestHeaderException exception.
     *
     * @param exception handled exception
     * @return response entity with error information and 400 status code
     */
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<PaymentRejectedResponse> handleMissingRequestHeaderException(
            MissingRequestHeaderException exception) {
        log.debug("Reason={}", exception.getMessage(), exception);

        var paymentRejectedResponse =
                PaymentRejectedResponse.builder()
                        .reason(exception.getMessage())
                        .reasonCode(ErrorReasonCode.INVALID_REQUEST)
                        .build();

        return ResponseEntity.badRequest()
                .body(paymentRejectedResponse);
    }

    /**
     * Handles AmountLimitExceededException exception.
     *
     * @param exception handled exception
     * @param request incoming request
     * @return response entity with error information and 422 status code
     */
    @ExceptionHandler(AmountLimitExceededException.class)
    public ResponseEntity<PaymentRejectedResponse> handleAmountLimitExceededException(
            AmountLimitExceededException exception,
            HttpServletRequest request) {
        var requestId = request.getHeader(Constants.X_REQUEST_ID_HEADER);

        log.debug("Request-id={}, reason={}", requestId, exception.getMessage(), exception);

        var paymentRejectedResponse =
                PaymentRejectedResponse.builder()
                        .reason(exception.getMessage())
                        .reasonCode(ErrorReasonCode.LIMIT_EXCEEDED)
                        .build();

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .header(Constants.X_REQUEST_ID_HEADER, requestId)
                .body(paymentRejectedResponse);
    }

    /**
     * Handles general exception.
     *
     * @param exception handled exception
     * @param request incoming request
     * @return response entity with error information and 500 status code
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<PaymentRejectedResponse> handleGeneralException(
            Exception exception,
            HttpServletRequest request) {
        var requestId = request.getHeader(Constants.X_REQUEST_ID_HEADER);

        log.debug("Request-id={}, reason={}", requestId, exception.getMessage(), exception);

        var paymentRejectedResponse =
                PaymentRejectedResponse.builder()
                        .reason(exception.getMessage())
                        .reasonCode(ErrorReasonCode.GENERAL_ERROR)
                        .build();

        return ResponseEntity.internalServerError()
                .header(Constants.X_REQUEST_ID_HEADER, requestId)
                .body(paymentRejectedResponse);
    }
}
