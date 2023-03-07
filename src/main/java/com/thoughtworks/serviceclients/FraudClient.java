package com.thoughtworks.serviceclients;

import com.thoughtworks.errorcodes.InternalErrorCodes;
import com.thoughtworks.exceptions.DependencyException;
import com.thoughtworks.payment.model.Payment;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Service
public class FraudClient {
    HttpClient client;

    @Value("${fraud.url}")
    private String baseUrl;


    @Autowired
    public FraudClient(HttpClient client) {
        this.client = client;
    }

    private static final String checkFraud = "/checkFraud";

    public boolean checkFraud(Payment payment) throws DependencyException {
        Map<String, String> paymentRequest = new HashMap<>() {{
            put("amount", String.valueOf(payment.getAmount()));
            put("beneficiaryName", payment.getBeneficiaryName());
            put("beneficiaryAccountNumber", String.valueOf(payment.getBeneficiaryAccountNumber()));
            put("beneficiaryIfscCode", payment.getBeneficiaryIfscCode());
            put("payeeName", payment.getPayeeName());
            put("payeeAccountNumber", String.valueOf(payment.getPayeeAccountNumber()));
            put("payeeIfscCode", payment.getPayeeIfscCode());

        }};

        try {
            int statusCode = client.post(String.format("%s%s", baseUrl, checkFraud), paymentRequest);

            if (statusCode == HttpStatus.SC_OK)
                return true;

            return false;
        } catch (IOException ex) {
            throw new DependencyException("ExternalService", InternalErrorCodes.SERVER_ERROR, baseUrl + "/checkFraud", "UNAVAILABLE", ex);
        }

    }
}
