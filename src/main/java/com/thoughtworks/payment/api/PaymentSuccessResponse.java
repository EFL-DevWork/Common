package com.thoughtworks.payment.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class PaymentSuccessResponse {
    @JsonProperty("statusMessage")
    private String statusMessage;
    @JsonProperty("paymentId")
    private Integer paymentId;

}



