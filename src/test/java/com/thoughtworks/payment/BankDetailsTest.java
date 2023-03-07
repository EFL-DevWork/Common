package com.thoughtworks.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.payment.api.BankDetails;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BankDetailsTest {

    @Test
    void expectsBankDetailsAfterSerialization() throws IOException {
        BankDetails details = new BankDetails("user1", 12345L, "HDFC1234");
        ObjectMapper objectMapper = new ObjectMapper();

        String detailsString = objectMapper.writeValueAsString(details);

        assertTrue(detailsString.contains("\"name\":\"user1\""));
        assertTrue(detailsString.contains("\"ifscCode\":\"HDFC1234\""));
        assertTrue(detailsString.contains("\"accountNumber\":12345"));
    }
}
