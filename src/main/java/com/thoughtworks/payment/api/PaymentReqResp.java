package com.thoughtworks.payment.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.thoughtworks.payment.model.Payment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;

@Getter
@NoArgsConstructor
public class PaymentReqResp {

    @JsonProperty("amount")
    @Schema(
            example = "100",
            required = true,
            maximum = "100000L"
    )
    @Max(100000L)
    private Integer amount;

    @JsonProperty("beneficiary")
    @Schema(required = true)
    @NotNull
    @Valid
    private BankDetails beneficiary;

    @JsonProperty("payee")
    @Schema(required = true)
    @NotNull
    @Valid
    private BankDetails payee;

    public PaymentReqResp(Payment payment) {
        this.amount = payment.getAmount();
        this.beneficiary = new BankDetails(payment.getBeneficiaryName(),
                payment.getBeneficiaryAccountNumber(),
                payment.getBeneficiaryIfscCode()
        );
        this.payee = new BankDetails(payment.getPayeeName(),
                payment.getPayeeAccountNumber(),
                payment.getPayeeIfscCode()
        );
    }

    public PaymentReqResp(Integer amount, BankDetails beneficiary, BankDetails payee) {
        this.amount = amount;
        this.beneficiary = beneficiary;
        this.payee = payee;
    }
}



