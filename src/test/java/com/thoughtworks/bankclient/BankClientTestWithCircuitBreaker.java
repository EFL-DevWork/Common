package com.thoughtworks.bankclient;

import com.thoughtworks.exceptions.DependencyException;
import com.thoughtworks.serviceclients.BankClient;
import com.thoughtworks.serviceclients.HttpClient;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
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

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
class BankClientTestWithCircuitBreaker {

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
    void circuitBreakerOpensAfterFiftyPercentThresholdFailureLimitAndDoesNotAllowRequests() throws IOException {
        doThrow(new IOException()).when(httpClient).get(anyString(), anyMap());

        assertThrows(DependencyException.class, () -> bankClient.checkBankDetails(12345, "HDFC1234", anyString()));
    }

    @Test
    void requestIsRetriedAfterReceivingDependencyException() throws IOException {
        doThrow(new IOException()).when(httpClient).get(anyString(), anyMap());

        assertThrows(DependencyException.class, () -> bankClient.checkBankDetails(12345, "HDFC1234", anyString()));
    }

    @Test
    void circuitBreakerChangesItsStateFromOpenToClosed() throws Exception {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("bankservice");

        doThrow(new IOException())
                .doThrow(new IOException())
                .doThrow(new IOException())
                .doThrow(new IOException())
                .doReturn(HttpStatus.SC_OK)
                .doReturn(HttpStatus.SC_OK)
                .doReturn(HttpStatus.SC_OK)
                .when(httpClient).get(anyString(), anyMap());

        assertThrows(DependencyException.class, () -> bankClient.checkBankDetails(12345, "HDFC1234", anyString()));
        assertEquals("CLOSED", circuitBreaker.getState().name());

        assertThrows(CallNotPermittedException.class, () -> bankClient.checkBankDetails(12345, "HDFC1234", anyString()));
        assertEquals("OPEN", circuitBreaker.getState().name());

        TimeUnit.SECONDS.sleep(5);
        assertThrows(DependencyException.class, () -> bankClient.checkBankDetails(12345, "HDFC1234", anyString()));
        assertEquals("HALF_OPEN", circuitBreaker.getState().name());

        assertThrows(CallNotPermittedException.class, () -> bankClient.checkBankDetails(12345, "HDFC1234", anyString()));
        assertEquals("OPEN", circuitBreaker.getState().name());

        TimeUnit.SECONDS.sleep(5);
        assertTrue(bankClient.checkBankDetails(12345, "HDFC1234", anyString()));
        assertEquals("HALF_OPEN", circuitBreaker.getState().name());

        assertTrue(bankClient.checkBankDetails(12345, "HDFC1234", anyString()));
        assertEquals("CLOSED", circuitBreaker.getState().name());

        assertTrue(bankClient.checkBankDetails(12345, "HDFC1234", anyString()));
        assertEquals("CLOSED", circuitBreaker.getState().name());
    }
}
