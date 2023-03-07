package com.thoughtworks.bankInfo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.bankInfo.api.BankInfoReqResp;
import com.thoughtworks.bankInfo.model.BankInfo;
import com.thoughtworks.errorcodes.InternalErrorCodes;
import com.thoughtworks.exceptions.ResourceConflictException;
import com.thoughtworks.filter.PrometheusFilterConfig;
import com.thoughtworks.handlers.ErrorResponse;
import com.thoughtworks.metrics.Prometheus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import javax.json.Json;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BankInfoController.class)
@Import({PrometheusFilterConfig.class})
class BankInfoControllerTest {

    public static final String BANKINFO = "/bankinfo";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BankInfoService bankInfoService;

    @MockBean
    Prometheus prometheus;

    private static BankInfo bankInfo;
    private static String bankInfoJson;
    private static ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll() throws JsonProcessingException {
        bankInfo = new BankInfo("HDFC", "http://localhost:8082");
        objectMapper = new ObjectMapper();
        bankInfoJson = objectMapper.writeValueAsString(bankInfo);
    }

    @Test
    void createBankInfo() throws Exception {
        BankInfoReqResp bankInfoResp = new BankInfoReqResp(bankInfo);
        when(bankInfoService.create(any(BankInfo.class))).thenReturn(bankInfo);

        mockMvc.perform(post(BANKINFO)
                .content(bankInfoJson)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(content().string(objectMapper.writeValueAsString(bankInfoResp)));

        verify(bankInfoService).create(any(BankInfo.class));
    }

    @Test
    void cannotCreateBankWhenBankInfoAlreadyExists() throws Exception {
        when(bankInfoService.create(any(BankInfo.class))).thenThrow(new ResourceConflictException(InternalErrorCodes.BANK_INFO_EXIST, InternalErrorCodes.BANK_INFO_EXIST.getDescription()));
        Map<String, String> errors = new HashMap<>();
        errors.put(InternalErrorCodes.BANK_INFO_EXIST.toString(), InternalErrorCodes.BANK_INFO_EXIST.getDescription());

        mockMvc.perform(post(BANKINFO)
                .content(bankInfoJson)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(content().string(objectMapper.writeValueAsString(new ErrorResponse().message("REQUEST_CONFLICT").reasons(errors))));

        verify(bankInfoService).create(any(BankInfo.class));
    }

    @Test
    void shouldReturnAllBanksInfo() throws Exception {
        BankInfo bankInfo2 = new BankInfo("AXIS1234", "http://bankservice:8082");
        bankInfoService.create(bankInfo);
        bankInfoService.create(bankInfo2);
        List<BankInfo> allBanks = new ArrayList<>();
        allBanks.add(bankInfo);
        allBanks.add(bankInfo2);
        when(bankInfoService.getAllBanks()).thenReturn(allBanks);

        ResultActions mockResult = mockMvc.perform(get(BANKINFO))
                .andExpect(status().is2xxSuccessful());
        String responseJson = mockResult.andReturn().getResponse().getContentAsString();
        List<BankInfo> bankInfoListResponse = new ObjectMapper().readValue(responseJson, objectMapper.getTypeFactory().constructCollectionType(List.class, BankInfo.class));

        verify(bankInfoService, times(1)).getAllBanks();
        assertEquals(2, bankInfoListResponse.size());
        assertEquals(bankInfo.getBankCode(), bankInfoListResponse.get(0).getBankCode());
        assertEquals(bankInfo2.getBankCode(), bankInfoListResponse.get(1).getBankCode());
    }
}
