package com.thoughtworks.serviceclients;

import com.thoughtworks.errorcodes.InternalErrorCodes;
import com.thoughtworks.exceptions.DependencyException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@Retry(name = " bankservice")
@CircuitBreaker(name = "bankservice")
public class BankClient {

    HttpClient client;

    @Autowired
    public BankClient(HttpClient client) {
        this.client = client;
    }

    private static final String checkDetails = "/checkDetails?";

    public boolean checkBankDetails(long accountNumber, String ifscCode, String baseUrl) throws DependencyException {
        try {
            Map<String, String> parameters = new HashMap<>();
            parameters.put("accountNumber", String.valueOf(accountNumber));
            parameters.put("ifscCode", ifscCode);

            int statusCode = client.get(String.format("%s%s", baseUrl, checkDetails), parameters);
            if (statusCode == HttpStatus.SC_OK) {
                return true;
            }
            return false;
        } catch (IOException ex) {
            throw new DependencyException("ExternalService", InternalErrorCodes.SERVER_ERROR, baseUrl + "/checkDetails", "UNAVAILABLE", ex);
        }
    }
}
