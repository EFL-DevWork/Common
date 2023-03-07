package com.thoughtworks.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thoughtworks.errorcodes.EventCodes;
import com.thoughtworks.handlers.ErrorResponse;
import com.thoughtworks.payment.api.PaymentReqResp;
import com.thoughtworks.payment.api.PaymentSuccessResponse;
import com.thoughtworks.payment.model.Payment;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@RestController
@Validated
@Tag(name = "payments")
public class PaymentController {
    private PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    public Optional<NativeWebRequest> getRequest() {
        return Optional.empty();
    }

    @Operation(
            operationId = "create",
            description = "Add a payment ",
            summary = "Make payment between two users",
            responses = {@ApiResponse(
                    responseCode = "400",
                    description = "Invalid input or request body missing or incorrect format",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ), @ApiResponse(
                    responseCode = "422",
                    description = "Payment request is unprocessable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ), @ApiResponse(
                    responseCode = "201",
                    description = "Payment created successfully",
                    content = @Content(schema = @Schema(implementation = PaymentSuccessResponse.class))
            ), @ApiResponse(
                    responseCode = "404",
                    description = "Info referred in request not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ), @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )},
            tags = {}
    )
    @PostMapping(value = "/payments", produces = {"application/json"}, consumes = {"application/json"})
    public ResponseEntity<PaymentSuccessResponse> create1(@Valid @RequestBody PaymentReqResp paymentRequest) throws Exception {
        Payment payment = new Payment(paymentRequest.getAmount(), paymentRequest.getBeneficiary(), paymentRequest.getPayee());
        Payment savedPayment = paymentService.create(payment);
        ObjectNode mapper = new ObjectMapper().createObjectNode();
        mapper.put("PaymentId", String.valueOf(savedPayment.getId()));
        mapper.put("BeneficiaryIfscCode", savedPayment.getBeneficiaryIfscCode());
        mapper.put("PayeeIfscCode", savedPayment.getPayeeIfscCode());
        log.info("payment successful", kv("event_code", EventCodes.PAYMENT_SUCCESSFUL), kv("details", mapper.toString()));
        PaymentSuccessResponse response = new PaymentSuccessResponse();
        response.setStatusMessage("Payment done successfully");
        response.setPaymentId(savedPayment.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(
            operationId = "getAllPayments",
            description = "Gets all payments ",
            summary = "Gets All Transactions done ",
            responses = {@ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ), @ApiResponse(
                    responseCode = "200",
                    description = "List of all payments",
                    content = {@Content(schema = @Schema(implementation = Payment.class))}
            )}
    )
    @GetMapping(value = "/payments", produces = {"application/json"})
    public ResponseEntity<List<PaymentReqResp>> getAllPayments() throws Exception {
        List<Payment> paymentList = paymentService.getAll();
        List<PaymentReqResp> paymentResponseList = paymentList.stream().map(payment -> new PaymentReqResp(payment)).collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.OK).body(paymentResponseList);
    }
}
