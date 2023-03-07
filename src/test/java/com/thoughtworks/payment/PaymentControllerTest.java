package com.thoughtworks.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.errorcodes.InternalErrorCodes;
import com.thoughtworks.exceptions.*;
import com.thoughtworks.handlers.ErrorResponse;
import com.thoughtworks.payment.api.BankDetails;
import com.thoughtworks.payment.api.PaymentReqResp;
import com.thoughtworks.payment.model.Payment;
import com.thoughtworks.payment.api.PaymentSuccessResponse;
import com.thoughtworks.metrics.Prometheus;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.metrics.CompositeMeterRegistryAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = PaymentController.class)
@Import({MetricsAutoConfiguration.class, CompositeMeterRegistryAutoConfiguration.class})
class PaymentControllerTest {

    public static final String PAYMENTS = "/payments";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    MeterRegistry meterRegistry;

    @MockBean
    PaymentService paymentService;

    @MockBean
    PaymentRepository paymentRepository;

    @MockBean
    Prometheus prometheus;

    @Autowired
    ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        when(prometheus.getPaymentsCounter()).thenReturn(Counter
                .builder("paymentService")
                .description("counter for number of payments")
                .tags("counter", "number of payments")
                .register(meterRegistry));
    }

    private Payment getPayment(int amount, Long bAccNo, String bIfscCode, Long pAccNo, String pIfscCode) {
        BankDetails beneficiary = new BankDetails("user1", bAccNo, bIfscCode);
        BankDetails payee = new BankDetails("user2", pAccNo, pIfscCode);
        return new Payment(amount, beneficiary, payee);
    }

    @Test
    void testGetAllPayments() throws Exception {
        List<Payment> paymentList = new ArrayList<>();
        Payment payment = getPayment(500, 12345L, "HDFC1234", 67890L, "HDFC1234");
        paymentList.add(payment);
        payment = getPayment(2100, 12345L, "HDFC1234", 67890L, "HDFC1234");
        paymentList.add(payment);
        when(paymentService.getAll()).thenReturn(paymentList);

        ResultActions mockResult = mockMvc.perform(get(PAYMENTS))
                .andExpect(status().is2xxSuccessful());
        String responseJson = mockResult.andReturn().getResponse().getContentAsString();
        List<PaymentReqResp> paymentListResponse = objectMapper.readValue(responseJson, objectMapper.getTypeFactory().constructCollectionType(List.class, PaymentReqResp.class));

        verify(paymentService, times(1)).getAll();
        assertEquals(2, paymentListResponse.size());
        assertEquals(500, paymentListResponse.get(0).getAmount().intValue());
        assertEquals(2100, paymentListResponse.get(1).getAmount().intValue());
    }

    @Test
    void shouldReturnSpecificHeadersForResponse() throws Exception {
        List<Payment> paymentList = new ArrayList<>();
        Payment payment = getPayment(500, 12345L, "HDFC1234", 67890L, "HDFC1234");
        paymentList.add(payment);
        payment = getPayment(2100, 12345L, "HDFC1234", 67890L, "HDFC1234");
        paymentList.add(payment);
        when(paymentService.getAll()).thenReturn(paymentList);

        ResultActions mockResult = mockMvc.perform(get(PAYMENTS))
                .andExpect(status().is2xxSuccessful());
        String cspHeader = mockResult.andReturn().getResponse().getHeader("Content-Security-Policy");
        String referrerPolicyHeader = mockResult.andReturn().getResponse().getHeader("Referrer-Policy");
        String strictTransportSecurityHeader = mockResult.andReturn().getResponse().getHeader("Strict-Transport-Security");

        assertEquals("script-src 'unsafe-inline' 'self'", cspHeader);
        assertEquals("strict-origin", referrerPolicyHeader);
        assertEquals("max-age=31536000 ; includeSubDomains", strictTransportSecurityHeader);
    }

    @Test
    void createPayment() throws Exception {
        Payment payment = getPayment(500, 12345L, "HDFC1234", 67890L, "HDFC1234");
        String paymentJson = objectMapper.writeValueAsString(new PaymentReqResp(payment));
        PaymentSuccessResponse response = new PaymentSuccessResponse();
        response.setStatusMessage("Payment done successfully");
        response.setPaymentId(payment.getId());
        when(paymentService.create(any(Payment.class))).thenReturn(payment);

        mockMvc.perform(post(PAYMENTS)
                .content(paymentJson)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(content().string(objectMapper.writeValueAsString(response)));

        verify(paymentService).create(any(Payment.class));
    }

    @Test
    void createPaymentWithBeneficiaryDetailsNotExists() throws Exception {
        Payment payment = getPayment(500, 12L, "HDFC1234", 67890L, "HDFC1234");
        String paymentJson = objectMapper.writeValueAsString(new PaymentReqResp(payment));
        Map<String, String> errors = new HashMap<>();
        errors.put(InternalErrorCodes.ACCOUNT_NOT_FOUND.toString(), "Beneficiary " + InternalErrorCodes.ACCOUNT_NOT_FOUND.getDescription());
        when(paymentService.create(any(Payment.class))).thenThrow(new ResourceNotFoundException(InternalErrorCodes.ACCOUNT_NOT_FOUND, "Beneficiary " + InternalErrorCodes.ACCOUNT_NOT_FOUND.getDescription()));

        mockMvc.perform(post(PAYMENTS)
                .content(paymentJson)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().string(objectMapper.writeValueAsString(new ErrorResponse().message("MISSING_INFO").reasons(errors))));

        verify(paymentService).create(any(Payment.class));
    }

    @Test
    void createPaymentWithPayeeDetailsNotExists() throws Exception {
        Payment payment = getPayment(500, 12345L, "HDFC1234", 12L, "HDFC1234");
        String paymentJson = objectMapper.writeValueAsString(new PaymentReqResp(payment));
        Map<String, String> errors = new HashMap<>();
        errors.put(InternalErrorCodes.ACCOUNT_NOT_FOUND.toString(), "Payee " + InternalErrorCodes.ACCOUNT_NOT_FOUND.getDescription());
        when(paymentService.create(any(Payment.class))).thenThrow(new ResourceNotFoundException(InternalErrorCodes.ACCOUNT_NOT_FOUND, "Payee " + InternalErrorCodes.ACCOUNT_NOT_FOUND.getDescription()));

        mockMvc.perform(post(PAYMENTS)
                .content(paymentJson)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().string(objectMapper.writeValueAsString(new ErrorResponse().message("MISSING_INFO").reasons(errors))));

        verify(paymentService).create(any(Payment.class));
    }

    @Test
    void createPaymentWithAmountGreaterThanLimit() throws Exception {
        Payment payment = getPayment(10000000, 12345L, "HDFC1234", 67890L, "HDFC1234");
        String paymentJson = objectMapper.writeValueAsString(new PaymentReqResp(payment));
        when(paymentService.create(any(Payment.class))).thenThrow(MethodArgumentNotValidException.class);

        mockMvc.perform(post(PAYMENTS)
                .content(paymentJson)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("{\"message\":\"INVALID_INPUT\",\"reasons\":{\"amount\":\"must be less than or equal to 100000\"}}"));

        verify(paymentService, times(0)).create(any(Payment.class));
    }

    @Test
    void createPaymentWithMultipleValidationErrors() throws Exception {
        when(paymentService.create(any(Payment.class))).thenThrow(MethodArgumentNotValidException.class);

        ResultActions mockResult = mockMvc.perform(post(PAYMENTS)
                .content(getContentForMultipleValidationErrors())
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(status().isBadRequest());
        String responseJson = mockResult.andReturn().getResponse().getContentAsString();
        ErrorResponse errorResponse = objectMapper.readValue(responseJson, ErrorResponse.class);

        assertErrorResponse(errorResponse);
        verify(paymentService, times(0)).create(any(Payment.class));
    }

    private String getContentForMultipleValidationErrors() {
        return "{\"amount\":10000000," +
                "\"beneficiary\":{\"name\":\"user1\",\"accountNumber\":12345,\"ifscCode\":null}" +
                ",\"payee\":{\"name\":\"user2\",\"accountNumber\":67890,\"ifscCode\":\"HDFC1234\"}" +
                "}";
    }

    private void assertErrorResponse(ErrorResponse errorResponse) {
        assertNotNull(errorResponse);
        boolean isAmountInvalid = false;
        boolean isBeneficiaryIfscCodeEmpty = false;

        Map<String, String> reasons = errorResponse.getReasons();
        for (Map.Entry<String, String> reason : reasons.entrySet()) {
            if (reason.getKey().equalsIgnoreCase("amount")
                    && reason.getValue().equalsIgnoreCase("must be less than or equal to 100000")) {
                isAmountInvalid = true;
            }
            if (reason.getKey().equalsIgnoreCase("beneficiary.ifscCode")
                    && reason.getValue().equalsIgnoreCase("must not be null")) {
                isBeneficiaryIfscCodeEmpty = true;
            }
        }

        assertTrue(isAmountInvalid);
        assertTrue(isBeneficiaryIfscCodeEmpty);

    }

    @Test
    void createPaymentWithBeneficiaryBankInfoMissing() throws Exception {
        Payment payment = getPayment(500, 12345L, "BANK1234", 67890L, "HDFC1234");
        String paymentJson = objectMapper.writeValueAsString(new PaymentReqResp(payment));
        Map<String, String> errors = new HashMap<>();
        errors.put(InternalErrorCodes.BANK_INFO_NOT_FOUND.toString(), InternalErrorCodes.BANK_INFO_NOT_FOUND.getDescription() + payment.getBeneficiaryIfscCode());
        when(paymentService.create(any(Payment.class))).thenThrow(new ResourceNotFoundException(InternalErrorCodes.BANK_INFO_NOT_FOUND, InternalErrorCodes.BANK_INFO_NOT_FOUND.getDescription() + payment.getBeneficiaryIfscCode()));

        mockMvc.perform(post(PAYMENTS)
                .content(paymentJson)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().string(objectMapper.writeValueAsString(new ErrorResponse().message("MISSING_INFO").reasons(errors))));

        verify(paymentService).create(any(Payment.class));
    }


    @Test
    void createPaymentWithInvalidIfscCodeFormat() throws Exception {
        Payment payment = getPayment(500, 12345L, "HDFC", 67890L, "HDFC1234");
        String paymentJson = objectMapper.writeValueAsString(new PaymentReqResp(payment));
        Map<String, String> errors = new HashMap<>();
        errors.put(InternalErrorCodes.INVALID_IFSC_FORMAT.toString(), InternalErrorCodes.INVALID_IFSC_FORMAT.getDescription() + payment.getBeneficiaryIfscCode());
        when(paymentService.create(any(Payment.class))).thenThrow(new ValidationException(InternalErrorCodes.INVALID_IFSC_FORMAT, InternalErrorCodes.INVALID_IFSC_FORMAT.getDescription() + payment.getBeneficiaryIfscCode()));

        mockMvc.perform(post(PAYMENTS)
                .content(paymentJson)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(objectMapper.writeValueAsString(new ErrorResponse().message("INVALID_INPUT").reasons(errors))));

        verify(paymentService).create(any(Payment.class));
    }


    @Test
    void createPaymentWithGeneralException() throws Exception {
        Payment payment = getPayment(500, 12345L, "HDFC1234", 12L, "HDFC1234");
        String paymentJson = objectMapper.writeValueAsString(new PaymentReqResp(payment));
        Map<String, String> errors = new HashMap<>();
        errors.put(InternalErrorCodes.SERVER_ERROR.toString(), InternalErrorCodes.SERVER_ERROR.getDescription());
        when(paymentService.create(any(Payment.class))).thenThrow(new Exception("Could not process the request"));
        mockMvc.perform(post(PAYMENTS)
                .content(paymentJson)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(objectMapper.writeValueAsString(new ErrorResponse().message("SERVER_ERROR").reasons(errors))));

        verify(paymentService).create(any(Payment.class));
    }

    @Test
    void testCannotCreatePaymentDueToSuspectedFraud() throws Exception {
        Payment payment = getPayment(500, 12345L, "HDFC1234", 12345L, "HDFC1234");
        String paymentJson = objectMapper.writeValueAsString(new PaymentReqResp(payment));
        Map<String, String> errors = new HashMap<>();
        errors.put(InternalErrorCodes.SUSPECTED_ACCOUNT.toString(), InternalErrorCodes.SUSPECTED_ACCOUNT.getDescription());
        when(paymentService.create(any(Payment.class))).thenThrow(new PaymentRefusedException(InternalErrorCodes.SUSPECTED_ACCOUNT, InternalErrorCodes.SUSPECTED_ACCOUNT.getDescription()));

        mockMvc.perform(post(PAYMENTS)
                .content(paymentJson)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string(objectMapper.writeValueAsString(new ErrorResponse().message("REQUEST_UNPROCESSABLE").reasons(errors))));

        verify(paymentService).create(any(Payment.class));
    }

    @Test
    void testCannotCreatePaymentDueToDependencyError() throws Exception {
        Payment payment = getPayment(500, 12345L, "HDFC1", 67L, "HDFC1234");
        String paymentJson = objectMapper.writeValueAsString(new PaymentReqResp(payment));
        Map<String, String> errors = new HashMap<>();
        errors.put(InternalErrorCodes.SERVER_ERROR.toString(), InternalErrorCodes.SERVER_ERROR.getDescription());
        when(paymentService.create(any(Payment.class))).thenThrow(new DependencyException("ExternalService", InternalErrorCodes.SERVER_ERROR, "/checkFraud", "UNAVAILABLE", new Exception()));

        mockMvc.perform(post(PAYMENTS)
                .content(paymentJson)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(objectMapper.writeValueAsString(new ErrorResponse().message("SERVER_ERROR").reasons(errors))));

        verify(paymentService).create(any(Payment.class));
    }

    @Test
    void testCannotCreatePaymentDownstreamError() throws Exception {
        Payment payment = getPayment(500, 12345L, "HDFC1234", 67890L, "HDFC1234");
        String paymentJson = objectMapper.writeValueAsString(new PaymentReqResp(payment));
        CircuitBreaker circuitBreaker = mock(CircuitBreaker.class);
        CircuitBreakerConfig config = mock(CircuitBreakerConfig.class);
        Map<String, String> errors = new HashMap<>();
        errors.put(InternalErrorCodes.SERVER_ERROR.toString(), InternalErrorCodes.SERVER_ERROR.getDescription());
        when(config.isWritableStackTraceEnabled()).thenReturn(false);
        when(circuitBreaker.getCircuitBreakerConfig()).thenReturn(config);
        CallNotPermittedException exception = CallNotPermittedException.createCallNotPermittedException(circuitBreaker);
        when(paymentService.create(any(Payment.class))).thenThrow(exception);

        mockMvc.perform(post(PAYMENTS)
                .content(paymentJson)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(objectMapper.writeValueAsString(new ErrorResponse().message("SERVER_ERROR").reasons(errors))));

        verify(paymentService).create(any(Payment.class));
    }

    @Test
    void testCannotCreatePaymentDueToBusinessError() throws Exception {
        Payment payment = getPayment(500, 12345L, "HDFC1", 67890L, "HDFC1234");
        String paymentJson = objectMapper.writeValueAsString(new PaymentReqResp(payment));
        when(paymentService.create(any(Payment.class))).thenThrow(new BusinessException(InternalErrorCodes.SUSPECTED_ACCOUNT, InternalErrorCodes.SUSPECTED_ACCOUNT.getDescription()));

        Map<String, String> errors = new HashMap<>();
        errors.put(InternalErrorCodes.SUSPECTED_ACCOUNT.toString(), InternalErrorCodes.SUSPECTED_ACCOUNT.getDescription());

        mockMvc.perform(post(PAYMENTS)
                .content(paymentJson)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string(objectMapper.writeValueAsString(new ErrorResponse().message("REQUEST_UNPROCESSABLE").reasons(errors))));

        verify(paymentService).create(any(Payment.class));
    }

    @Test
    void createPaymentWithRequestBodyMissing() throws Exception {
        Map<String, String> errors = new HashMap<>();
        errors.put(InternalErrorCodes.PAYMENT_REQUEST_NOT_READABLE.toString(), InternalErrorCodes.PAYMENT_REQUEST_NOT_READABLE.getDescription());

        mockMvc.perform(post(PAYMENTS)
                .content("")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(objectMapper.writeValueAsString(new ErrorResponse().message("INVALID_INPUT").reasons(errors))));
    }

    @Test
    void createPaymentWithRequestBodyIsInIncorrectFormat() throws Exception {
        Map<String, String> errors = new HashMap<>();
        errors.put(InternalErrorCodes.PAYMENT_REQUEST_NOT_READABLE.toString(), InternalErrorCodes.PAYMENT_REQUEST_NOT_READABLE.getDescription());

        mockMvc.perform(post(PAYMENTS)
                .content("{\"amount\":500," +
                        "\"beneficiary\":{\"name\":\"user1\"\"accountNumber\":12345ifscCode\":\"HDFC1\"}" +
                        ",\"payee\":{\"name\":\"user2\",\"accountNumber\":12345,\"ifscCode\":\"HDFC1234\"}" +
                        "}")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(objectMapper.writeValueAsString(new ErrorResponse().message("INVALID_INPUT").reasons(errors))));
    }
}
