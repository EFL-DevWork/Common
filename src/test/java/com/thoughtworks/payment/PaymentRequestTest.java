package com.thoughtworks.payment;

import com.thoughtworks.payment.api.BankDetails;
import com.thoughtworks.payment.api.PaymentReqResp;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class PaymentRequestTest {

    @Test
    public void createPaymentWithAmountGreaterThanLimit() throws Exception {

        BankDetails payee = new BankDetails("user2", 67890L, "HDFC1234");

        Set<ConstraintViolation<PaymentReqResp>> violations = CheckPaymentRequest(new PaymentReqResp(10000000, null, payee));

        ConstraintViolation<PaymentReqResp> amountLimitViolation = null;
        ConstraintViolation<PaymentReqResp> beneficiaryNullViolation = null;

        for (ConstraintViolation<PaymentReqResp> violation : violations) {
            if (String.valueOf(violation.getPropertyPath()).equals("amount")) amountLimitViolation = violation;
            if (String.valueOf(violation.getPropertyPath()).equals("beneficiary")) beneficiaryNullViolation = violation;
        }
        assertNotNull(amountLimitViolation);
        assertNotNull(beneficiaryNullViolation);
        assertEquals("must be less than or equal to 100000", amountLimitViolation.getMessage());
        assertEquals("must not be null", beneficiaryNullViolation.getMessage());
    }

    private Set<ConstraintViolation<PaymentReqResp>> CheckPaymentRequest(PaymentReqResp paymentRequest) throws Exception {
        ValidatorFactory vf = Validation.buildDefaultValidatorFactory();
        Validator validator = vf.getValidator();
        return validator.validate(paymentRequest);
    }

}
