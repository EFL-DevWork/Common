package com.thoughtworks.bankclient;


import com.thoughtworks.exceptions.DependencyException;
import com.thoughtworks.exceptions.ResourceNotFoundException;
import com.thoughtworks.exceptions.ValidationException;
import com.thoughtworks.serviceclients.BankClient;
import com.thoughtworks.serviceclients.HttpClient;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import io.opentelemetry.api.GlobalOpenTelemetry;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.client.RestTemplate;

import javax.swing.*;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
class BankClientTest {

    @Autowired
    BankClient bankClient;

    @Autowired
    CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired
    RetryRegistry retryRegistry;

    @MockBean
    RestTemplate restTemplate;

    @MockBean
    HttpClient httpClient;

    @BeforeAll
    static void beforeAll() {
        GlobalOpenTelemetry.resetForTest();
    }

    @BeforeEach
    void circuitBreakerSetup() {
        circuitBreakerRegistry.circuitBreaker("bankservice").reset();
    }

    @AfterEach
    void clearMDC() {
        MDC.clear();
    }

    @Test
    void testCheckBankDetailsSuccess() throws Exception {
        when(httpClient.get(anyString(), anyMap())).thenReturn(HttpStatus.SC_OK);

        assertTrue(bankClient.checkBankDetails(12345, "HDFC1234", anyString()));
    }

    @Test
    void testCheckBankDetailsWithWrongIfscCode() throws Exception {
        doThrow(new IOException()).when(httpClient).get(anyString(), anyMap());
        assertThrows(DependencyException.class, () -> bankClient.checkBankDetails(12345, "XXYY1234", anyString()));
    }

    @Test
    void testCheckBankDetailsWithInvalidFormatIfscCode() throws Exception {
        Runnable circuitBreakerReset = () -> {
            circuitBreakerRegistry.circuitBreaker("bankservice").reset();
        };

        doThrow(new IOException())
                .doThrow(new IOException())
                .doThrow(new IOException())
                .doThrow(new IOException())
                .when(httpClient).get(anyString(), anyMap());

        assertThrows(DependencyException.class, () -> bankClient.checkBankDetails(12345, "", anyString()));
        circuitBreakerReset.run();
        assertThrows(DependencyException.class, () -> bankClient.checkBankDetails(12345, "H", anyString()));
        circuitBreakerReset.run();
        assertThrows(DependencyException.class, () -> bankClient.checkBankDetails(12345, "HDFC", anyString()));
        circuitBreakerReset.run();
        assertThrows(DependencyException.class, () -> bankClient.checkBankDetails(12345, null, anyString()));
    }

    @Test
    void testCheckBankDetailsForInvalidAccount() throws Exception {
        when(httpClient.get(anyString(), anyMap())).thenReturn(HttpStatus.SC_NOT_FOUND);

        assertEquals(false, bankClient.checkBankDetails(0, "HDFC1234", anyString()));
    }

    @Test
    void testCheckBankDetailsForWrongBaseUrl() throws IOException, DependencyException {
        when(httpClient.get(anyString(), anyMap())).thenReturn(HttpStatus.SC_NOT_FOUND);

        assertFalse(bankClient.checkBankDetails(1234, "HDFC1234", anyString()));
    }
}
