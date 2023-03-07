package com.thoughtworks.bankInfo;


import com.thoughtworks.bankInfo.model.BankInfo;
import org.junit.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BankInfoMapperTest {

    @Test
    public void shouldMapBankInfo(){
        BankInfo bankInfo = new BankInfo("bank-code", "bank-url");

        assertEquals(bankInfo.getId(), bankInfo.getId());
        assertEquals("bank-code", bankInfo.getBankCode());
        assertEquals("bank-url", bankInfo.getUrl());
    }

}