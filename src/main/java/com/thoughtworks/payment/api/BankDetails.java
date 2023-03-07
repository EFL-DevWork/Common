package com.thoughtworks.payment.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BankDetails {

    @JsonProperty("name")
    @Schema(example = "user", required = true)
    @NotNull
    private String name;

    @JsonProperty("accountNumber")
    @Schema(example = "12345", required = true)
    @NotNull
    private Long accountNumber;

    @JsonProperty("ifscCode")
    @Schema(example = "HDFC1234", required = true)
    @NotNull
    private String ifscCode;
}



