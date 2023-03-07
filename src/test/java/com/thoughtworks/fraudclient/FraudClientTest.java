package com.thoughtworks.fraudclient;

import com.thoughtworks.payment.api.BankDetails;
import com.thoughtworks.errorcodes.InternalErrorCodes;
import com.thoughtworks.exceptions.DependencyException;
import com.thoughtworks.payment.model.Payment;
import com.thoughtworks.serviceclients.FraudClient;
import com.thoughtworks.serviceclients.HttpClient;
import io.opentelemetry.api.GlobalOpenTelemetry;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class FraudClientTest {

    @Autowired
    FraudClient fraudClient;

    @MockBean
    RestTemplate restTemplate;

    @MockBean
    HttpClient httpClient;


    @BeforeAll
    static void beforeAll() {
        GlobalOpenTelemetry.resetForTest();
    }

    private BankDetails createBankDetails(String name, Long accountNumber, String ifscCode) {
        return new BankDetails(name, accountNumber, ifscCode);
    }

    private Payment createPayment(int amount, BankDetails beneficiary, BankDetails payee) {
        return new Payment(amount, beneficiary, payee);
    }

    @Test
    void checkClientWhenNoFraudReturned() throws Exception {
        when(httpClient.post(anyString(), anyMap())).thenReturn(HttpStatus.SC_OK);

        Payment payment = createPayment(100,
                createBankDetails("user1", 12345L, "HDFC1234"),
                createBankDetails("user2", 67890L, "HDFC1234")
        );

        assertTrue(fraudClient.checkFraud(payment));
    }

    @Test
    void checkClientWhenFraudReturned() throws Exception {
        when(httpClient.post(anyString(), anyMap())).thenReturn(HttpStatus.SC_UNPROCESSABLE_ENTITY);

        Payment payment = createPayment(100,
                createBankDetails("user1", 12345L, "HDFC1234"),
                createBankDetails("user2", 12345L, "HDFC1234")
        );

        assertFalse(fraudClient.checkFraud(payment));
    }

    @Test
    void checkClientWhenServerErrorReturned() throws Exception {
        when(httpClient.post(anyString(), anyMap())).thenReturn(HttpStatus.SC_INTERNAL_SERVER_ERROR);

        doThrow(new IOException()).when(httpClient).post(anyString(), anyMap());
        Payment payment = createPayment(100,
                createBankDetails("user1", 12345L, "HDFC1234"),
                createBankDetails("user2", 12345L, "HDFC1234")
        );

        DependencyException dex = assertThrows(DependencyException.class, () -> fraudClient.checkFraud(payment));
        assertEquals(InternalErrorCodes.SERVER_ERROR, dex.getErrorCode());
        assertEquals("UNAVAILABLE", dex.getErrorMessage());
    }

    @Test
    void checkClientWhenServiceNotReachable() throws Exception {
        when(httpClient.post(anyString(), anyMap())).thenReturn(HttpStatus.SC_NOT_FOUND);

        doThrow(IOException.class).when(httpClient).post(anyString(), anyMap());

        Payment payment = createPayment(100,
                createBankDetails("user1", 12345L, "HDFC1234"),
                createBankDetails("user2", 12345L, "HDFC1234")
        );

        DependencyException dex = assertThrows(DependencyException.class, () -> fraudClient.checkFraud(payment));
        assertEquals(InternalErrorCodes.SERVER_ERROR, dex.getErrorCode());
        assertEquals("UNAVAILABLE", dex.getErrorMessage());
    }
}
