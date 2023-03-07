package com.thoughtworks.handlers;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import javax.validation.Valid;
import java.util.Map;

@Getter
public class ErrorResponse {
    @JsonProperty("message")
    @Schema
    private String message;
    @JsonProperty("reasons")
    @Valid
    private Map<String, String> reasons = null;

    public ErrorResponse() {
    }

    public ErrorResponse message(String message) {
        this.message = message;
        return this;
    }

    public ErrorResponse reasons(Map<String, String> reasons) {
        this.reasons = reasons;
        return this;
    }

    @Schema
    public Map<String, String> getReasons() {
        return this.reasons;
    }
}
