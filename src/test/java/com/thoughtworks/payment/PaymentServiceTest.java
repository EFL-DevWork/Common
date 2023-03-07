package com.thoughtworks.payment;

import com.thoughtworks.bankInfo.BankInfoService;
import com.thoughtworks.bankInfo.model.BankInfo;
import com.thoughtworks.exceptions.DependencyException;
import com.thoughtworks.exceptions.PaymentRefusedException;
import com.thoughtworks.exceptions.ResourceNotFoundException;
import com.thoughtworks.payment.api.BankDetails;
import com.thoughtworks.payment.api.PaymentReqResp;
import com.thoughtworks.payment.model.Payment;
import com.thoughtworks.serviceclients.BankClient;
import com.thoughtworks.serviceclients.FraudClient;
import io.opentelemetry.api.GlobalOpenTelemetry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
class PaymentServiceTest {

    @Autowired
    PaymentService paymentService;

    @Autowired
    PaymentRepository paymentRepository;

    @MockBean
    BankClient bankClient;

    @MockBean
    FraudClient fraudClient;

    @MockBean
    RestTemplate restTemplate;

    @MockBean
    BankInfoService bankService;

    @BeforeAll
    static void beforeAll() {
        GlobalOpenTelemetry.resetForTest();
    }

    @AfterEach
    void tearDown() {
        paymentRepository.deleteAll();
    }

    @Test
    void createAPayment() throws Exception {
        BankDetails beneficiary = new BankDetails("user1", 12345L, "HDFC1234");
        BankDetails payee = new BankDetails("user2", 67890L, "HDFC1234");
        PaymentReqResp paymentRequest = new PaymentReqResp(100, beneficiary, payee);
        Payment payment = new Payment(paymentRequest.getAmount(), paymentRequest.getBeneficiary(), paymentRequest.getPayee());
        when(bankClient.checkBankDetails(anyLong(), anyString(), anyString())).thenReturn(true);
        when(fraudClient.checkFraud(any())).thenReturn(true);
        when(bankService.fetchBankByBankCode(anyString())).thenReturn(new BankInfo("HDFC", "http://localhost:9001"));

        Payment savedPayment = paymentService.create(payment);

        assertEquals(100, savedPayment.getAmount());
        assertEquals(beneficiary.getName(), savedPayment.getBeneficiaryName());
        assertEquals(beneficiary.getAccountNumber(), savedPayment.getBeneficiaryAccountNumber());
        assertEquals(beneficiary.getIfscCode(), savedPayment.getBeneficiaryIfscCode());
        assertEquals(payee.getName(), savedPayment.getPayeeName());
        assertEquals(payee.getAccountNumber(), savedPayment.getPayeeAccountNumber());
        assertEquals(payee.getIfscCode(), savedPayment.getPayeeIfscCode());

    }

    @Test
    void paymentTransactionFailsIfBeneficiaryAccountNotFound() throws Exception {
        BankDetails beneficiary = new BankDetails("user1", 00000L, "OOOOOO");
        BankDetails payee = new BankDetails("user2", 67890L, "HDFC1234");
        PaymentReqResp paymentRequest = new PaymentReqResp(100, beneficiary, payee);
        Payment payment = new Payment(paymentRequest.getAmount(), paymentRequest.getBeneficiary(), paymentRequest.getPayee());
        when(bankService.fetchBankByBankCode(anyString())).thenReturn(new BankInfo("HDFC", "http://localhost:9001"));
        when(bankClient.checkBankDetails(anyLong(), anyString(), anyString())).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> paymentService.create(payment));

        assertEquals("user1's AccountDetails Not Found At OOOOOO", exception.getErrorMessage());
    }

    @Test
    void paymentTransactionFailsIfPayeeAccountNotFound() throws Exception {
        BankDetails beneficiary = new BankDetails("user1", 12345L, "HDFC1234");
        BankDetails payee = new BankDetails("user2", 00000L, "0000000");
        PaymentReqResp paymentRequest = new PaymentReqResp(100, beneficiary, payee);
        Payment payment = new Payment(paymentRequest.getAmount(), paymentRequest.getBeneficiary(), paymentRequest.getPayee());
        when(bankClient.checkBankDetails(anyLong(), anyString(), anyString())).thenReturn(true, false);
        when(bankService.fetchBankByBankCode(anyString())).thenReturn(new BankInfo("HDFC", "http://localhost:9001"));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> paymentService.create(payment));

        assertEquals("user2's AccountDetails Not Found At 0000000", exception.getErrorMessage());
    }

    @Test
    void testCreatePaymentForInvalidArguments() {
        BankDetails beneficiary = new BankDetails("user1", 12345L, "HDFC1234");
        BankDetails payee = new BankDetails("user2", 67890L, "HDFC1234");

        assertThrows(IllegalArgumentException.class, () -> paymentService.create(null));
        assertThrows(IllegalArgumentException.class, () -> paymentService.create(new Payment(0, beneficiary, payee)));
        assertThrows(IllegalArgumentException.class, () -> paymentService.create(new Payment(100, null, payee)));
        assertThrows(IllegalArgumentException.class, () -> paymentService.create(new Payment(100, beneficiary, null)));

    }

    @Test
    void testCreatePaymentForMissingBankDetails() throws Exception {
        BankDetails validBankDetails = new BankDetails("user1", 12345L, "HDFC1234");
        BankDetails bankDetailsWithInvalidIfscCode = new BankDetails("user2", 67890L, "AAAAAAA");
        PaymentReqResp payeeWithInvalidBankDetails = new PaymentReqResp(100, bankDetailsWithInvalidIfscCode, validBankDetails);
        PaymentReqResp payeeWithValidBankDetails = new PaymentReqResp(100, validBankDetails, bankDetailsWithInvalidIfscCode);
        when(bankClient.checkBankDetails(anyLong(), eq("AAAAAAA"), anyString())).thenThrow(DependencyException.class);
        when(bankClient.checkBankDetails(anyLong(), eq("HDFC1234"), anyString())).thenReturn(true);

        assertThrows(ResourceNotFoundException.class, () -> paymentService.create(new Payment(payeeWithInvalidBankDetails.getAmount(), payeeWithInvalidBankDetails.getBeneficiary(), payeeWithInvalidBankDetails.getPayee())));
        assertThrows(ResourceNotFoundException.class, () -> paymentService.create(new Payment(payeeWithValidBankDetails.getAmount(), payeeWithValidBankDetails.getBeneficiary(), payeeWithValidBankDetails.getPayee())));
    }

    @Test
    void testCreatePaymentWithFraudulentInput() throws Exception {
        BankDetails beneficiary = new BankDetails("user1", 12345L, "HDFC1234");
        BankDetails payee = new BankDetails("user2", 12345L, "HDFC1234");
        PaymentReqResp paymentRequest = new PaymentReqResp(100, beneficiary, payee);
        Payment payment = new Payment(paymentRequest.getAmount(), paymentRequest.getBeneficiary(), paymentRequest.getPayee());
        when(bankClient.checkBankDetails(anyLong(), anyString(), anyString())).thenReturn(true);
        when(fraudClient.checkFraud(any())).thenReturn(false);
        when(bankService.fetchBankByBankCode(anyString())).thenReturn(new BankInfo("HDFC", "http://localhost:9001"));

        assertThrows(PaymentRefusedException.class, () -> paymentService.create(payment));
    }

    @Test
    void testFindAll() throws Exception {

        BankDetails beneficiary = new BankDetails("user1", 12345L, "HDFC1234");
        BankDetails payee = new BankDetails("user2", 67890L, "HDFC1234");
        when(bankClient.checkBankDetails(anyLong(), anyString(), anyString())).thenReturn(true);
        when(fraudClient.checkFraud(any())).thenReturn(true);
        when(bankService.fetchBankByBankCode(anyString())).thenReturn(new BankInfo("HDFC", "http://localhost:9001"));
        for (int i = 0; i < 10; i++) {
            PaymentReqResp paymentRequest = new PaymentReqResp(100 + i * 10, beneficiary, payee);
            Payment payment = new Payment(paymentRequest.getAmount(), paymentRequest.getBeneficiary(), paymentRequest.getPayee());
            paymentService.create(payment);
        }

        List<Payment> allPayments = paymentService.getAll();

        assertEquals(10, allPayments.size());
        for (int i = 0; i < allPayments.size(); i++) {
            assertEquals(i + 1, allPayments.get(i).getId());
        }
    }
}
