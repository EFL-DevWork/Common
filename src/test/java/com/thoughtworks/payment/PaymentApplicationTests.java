package com.thoughtworks.payment;

import io.opentelemetry.api.GlobalOpenTelemetry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
class PaymentApplicationTests {


    @MockBean
    RestTemplate restTemplate;

    @BeforeAll
    static void beforeAll() {
        GlobalOpenTelemetry.resetForTest();
    }

    @Test
    void contextLoads() {
    }
}
