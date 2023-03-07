package com.thoughtworks.payment;

import com.google.gson.JsonObject;
import com.thoughtworks.bankInfo.BankInfoService;
import com.thoughtworks.bankInfo.model.BankInfo;
import com.thoughtworks.errorcodes.InternalErrorCodes;
import com.thoughtworks.exceptions.DependencyException;
import com.thoughtworks.exceptions.PaymentRefusedException;
import com.thoughtworks.exceptions.ResourceNotFoundException;
import com.thoughtworks.exceptions.ValidationException;
import com.thoughtworks.metrics.Prometheus;
import com.thoughtworks.payment.model.Payment;
import com.thoughtworks.serviceclients.BankClient;
import com.thoughtworks.serviceclients.FraudClient;
import com.thoughtworks.serviceclients.util.TracingUtil;
import io.micrometer.core.instrument.Counter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class PaymentService {
    PaymentRepository paymentRepository;

    BankClient bankClient;

    FraudClient fraudClient;

    Prometheus prometheus;

    BankInfoService bankService;

    TracingUtil tracingUtil;

    private Counter bankCounter;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository,
                          BankClient bankClient,
                          FraudClient fraudClient,
                          Prometheus prometheus,
                          BankInfoService bankService,
                          TracingUtil tracingUtil) {
        this.paymentRepository = paymentRepository;
        this.bankClient = bankClient;
        this.fraudClient = fraudClient;
        this.prometheus = prometheus;
        this.bankService = bankService;
        this.tracingUtil = tracingUtil;
    }


    public Payment create(Payment payment) throws Exception {
        boolean isValidBeneficiaryAccount;
        boolean isValidPayeeAccount;
        if (payment == null) {
            throw new IllegalArgumentException("paymentRequest cannot be null");
        }

        BankInfo bankInfo = bankService.fetchBankByBankCode(getBankCode(payment.getBeneficiaryIfscCode()));
        if (bankInfo == null)
            throw new ResourceNotFoundException(InternalErrorCodes.BANK_INFO_NOT_FOUND, InternalErrorCodes.BANK_INFO_NOT_FOUND.getDescription() + payment.getBeneficiaryIfscCode());
        String baseUrl = bankInfo.getUrl();

        Context ctx = Context.current();

        checkDetailsFor("beneficiary", payment, baseUrl, ctx);

        checkDetailsFor("payee", payment, baseUrl, ctx);

        Span span = tracingUtil.startSpan("checkFraud", ctx);
        boolean isValidPayment = fraudClient.checkFraud(payment);
        if (!isValidPayment) {
            payment.setStatus("failed");
            paymentRepository.save(payment);
            span.end();
            throw new PaymentRefusedException(InternalErrorCodes.SUSPECTED_ACCOUNT, "Suspected fraudulent transaction");
        }
        span.end();

        bankCounter = prometheus.getBankInfoCounter(payment.getBeneficiaryIfscCode().substring(0, 4));
        bankCounter.increment();
        return paymentRepository.save(payment);
    }

    private void checkDetailsFor(String s, Payment payment, String baseUrl, Context ctx) throws DependencyException, ResourceNotFoundException {
        Span span = tracingUtil.startSpan("checkBankDetails", ctx);
        boolean isValid = (Objects.equals(s, "beneficiary")) ?
                bankClient.checkBankDetails(payment.getBeneficiaryAccountNumber(), payment.getBeneficiaryIfscCode(), baseUrl)
                : bankClient.checkBankDetails(payment.getPayeeAccountNumber(), payment.getPayeeIfscCode(), baseUrl);

        if (!isValid) {
            payment.setStatus("failed");
            paymentRepository.save(payment);

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("PaymentId", String.valueOf(payment.getId()));
            jsonObject.addProperty("BeneficiaryIfscCode", payment.getBeneficiaryIfscCode());
            jsonObject.addProperty("PayeeIfscCode", payment.getPayeeIfscCode());

            span.end();
            String errMsg = (Objects.equals(s, "beneficiary")) ? payment.getBeneficiaryName() + "'s AccountDetails Not Found At " + payment.getBeneficiaryIfscCode()
                    : payment.getPayeeName() + "'s AccountDetails Not Found At " + payment.getPayeeIfscCode();
            throw new ResourceNotFoundException(InternalErrorCodes.ACCOUNT_NOT_FOUND, errMsg, jsonObject);
        }

        span.end();
    }

    public List<Payment> getAll() {
        return paymentRepository.findAll();
    }

    private String getBankCode(String ifscCode) throws ValidationException {
        if (ifscCode != null && ifscCode.length() >= 5) {
            return ifscCode.substring(0, 4);
        } else {
            throw new ValidationException(InternalErrorCodes.INVALID_IFSC_FORMAT, "Invalid ifscCode format ->" + ifscCode);
        }
    }

}

