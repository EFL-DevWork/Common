package com.thoughtworks.payment;

import com.thoughtworks.payment.api.BankDetails;
import com.thoughtworks.payment.model.Payment;
import org.junit.Test;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.*;

public class PaymentMapperTest {

    @Test
    public void shouldMapPaymentDetails(){
        BankDetails beneficiary = new BankDetails("user1", 12L, "HDFC1");
        BankDetails payee = new BankDetails("user2", 12346L, "HDFC1234");
        String requestId = "xxxx-1111-2222";
        MDC.put("trace_id", requestId);
        int amount = 500;
        Payment payment = new Payment(amount, beneficiary, payee);

        assertEquals(amount, payment.getAmount());
        assertEquals("success", payment.getStatus());
        assertEquals(requestId, payment.getRequestId());
        assertEquals(beneficiary.getName(), payment.getBeneficiaryName());
        assertEquals(beneficiary.getAccountNumber(), payment.getBeneficiaryAccountNumber());
        assertEquals(beneficiary.getIfscCode(), payment.getBeneficiaryIfscCode());
        assertEquals(payee.getName(), payment.getPayeeName());
        assertEquals(payee.getAccountNumber(), payment.getPayeeAccountNumber());
        assertEquals(payee.getIfscCode(), payment.getPayeeIfscCode());
    }

}