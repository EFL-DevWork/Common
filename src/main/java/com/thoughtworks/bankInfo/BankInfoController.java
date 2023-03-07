package com.thoughtworks.bankInfo;

import com.thoughtworks.bankInfo.api.BankInfoReqResp;
import com.thoughtworks.bankInfo.model.BankInfo;
import com.thoughtworks.exceptions.ResourceConflictException;
import com.thoughtworks.handlers.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Validated
@Tag(name = "bankinfo")
public class BankInfoController {

    BankInfoService bankInfoService;

    @Autowired
    public BankInfoController(BankInfoService bankInfoService) {
        this.bankInfoService = bankInfoService;
    }

    @Operation(
            description = "Add a bankinfo ",
            operationId = "create",
            summary = "Adds the new bank and its url",
            responses = {@ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ), @ApiResponse(
                    responseCode = "201",
                    description = "BankInfo created successfully",
                    content = @Content(schema = @Schema(implementation = BankInfo.class))
            )},
            tags = {}
    )
    @PostMapping(value = "/bankinfo", consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<BankInfoReqResp> create(@RequestBody BankInfoReqResp request) throws ResourceConflictException {
        BankInfo bankInfo = bankInfoService.create(new BankInfo(request.getBankCode(), request.getUrl()));
        BankInfoReqResp bankInfoResp = new BankInfoReqResp(bankInfo);
        return new ResponseEntity<>(bankInfoResp, HttpStatus.CREATED);
    }

    @Operation(
            description = "Gets all banks ",
            operationId = "getAllBanks",
            summary = "Gets all banks information ",

            responses = {@ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ), @ApiResponse(
                    responseCode = "200",
                    description = "List of all banks",
                    content = {@Content(array = @ArraySchema(schema = @Schema(implementation = BankInfo.class)))})}
   )
    @GetMapping(value = "/bankinfo", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<BankInfoReqResp>> getAllBanks() {
        List<BankInfo> bankList = bankInfoService.getAllBanks();
        List<BankInfoReqResp> bankRespList = bankList.stream().map(BankInfoReqResp::new).collect(Collectors.toList());
        return new ResponseEntity<>(bankRespList, HttpStatus.OK);
    }
}
