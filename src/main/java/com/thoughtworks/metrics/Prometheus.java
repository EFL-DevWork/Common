package com.thoughtworks.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


@Service
public class Prometheus {
    private Counter paymentsCounter;
    private Map<String, Counter> bankInfoCounterList = new HashMap<>();
    MeterRegistry meterRegistry;

    @Autowired
    public Prometheus(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        paymentsCounter = Counter
                .builder("paymentService")
                .description("counter for number of payments")
                .tags("counter", "number of payments")
                .register(meterRegistry);

    }

    public Counter getPaymentsCounter() {
        return paymentsCounter;
    }

    public Counter getBankInfoCounter(String bankName) {
        if (bankInfoCounterList.containsKey(bankName)) {
            return bankInfoCounterList.get(bankName);
        } else {
            Counter tempBankInfoCounter = Counter
                    .builder(bankName)
                    .description("count payments for " + bankName)
                    .tags("counter", "successful payments with " + bankName)
                    .register(meterRegistry);

            bankInfoCounterList.put(bankName, tempBankInfoCounter);
            return tempBankInfoCounter;
        }
    }
}
