package com.thoughtworks.bankInfo.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.thoughtworks.bankInfo.model.BankInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class BankInfoReqResp {
    @JsonProperty("bankCode")
    @Schema(description = "gets bank code", example = "HDFC")
    private String bankCode;
    @JsonProperty("url")
    @Schema(description = "gets bank service url", example="hdfcbank.com")
    private String url;

    public BankInfoReqResp(BankInfo bankInfo) {
        this.bankCode = bankInfo.getBankCode();
        this.url = bankInfo.getUrl();
    }
}


