package com.thoughtworks.bankInfo;


import com.thoughtworks.bankInfo.model.BankInfo;
import com.thoughtworks.exceptions.ResourceConflictException;
import io.opentelemetry.api.GlobalOpenTelemetry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class BankInfoServiceTest {

    @Autowired
    BankInfoService bankInfoService;

    @Autowired
    BankInfoRepository bankInfoRepository;

    @BeforeAll
    static void beforeAll() {
        GlobalOpenTelemetry.resetForTest();
    }

    @BeforeEach
    void tearDown() {
        bankInfoRepository.deleteAll();
    }

    @Test
    void createBankTest() throws ResourceConflictException {
        BankInfo bank = new BankInfo("HDFC", "hdfc-url");
        BankInfo savedBank = bankInfoService.create(bank);

        assertEquals(bank.getBankCode(), savedBank.getBankCode());
        assertEquals(bank.getUrl(), savedBank.getUrl());
    }

    @Test
    void cannotCreateBankWhenBankInfoAlreadyExists() throws ResourceConflictException {
        BankInfo bank = new BankInfo("HDFC", "hdfc-url");
        bankInfoService.create(bank);

        ResourceConflictException exception =
                assertThrows(ResourceConflictException.class, () -> bankInfoService.create(bank));

        assertEquals("Bank info already exists", exception.getErrorMessage());
    }

    @Test
    void cannotCreateBankWhenBankCodeIsNull() {
        assertThrows(IllegalArgumentException.class, () -> bankInfoService.create(new BankInfo(null, "http://localhost:8082")));
        assertThrows(IllegalArgumentException.class, () -> bankInfoService.create(new BankInfo("HDFC", null)));
        assertThrows(IllegalArgumentException.class, () -> bankInfoService.create(null));
        assertThrows(IllegalArgumentException.class, () -> bankInfoService.create(new BankInfo("", "http://localhost:8082")));
        assertThrows(IllegalArgumentException.class, () -> bankInfoService.create(new BankInfo("HDFC", "")));
    }


    @Test
    void fetchABankByBankCodeTest() throws ResourceConflictException {
        BankInfo bank = new BankInfo("HDFC", "hdfc-url");
        BankInfo savedBank = bankInfoService.create(bank);
        BankInfo fetchedBank = bankInfoService.fetchBankByBankCode("HDFC");

        assertEquals(savedBank.getBankCode(), fetchedBank.getBankCode());
        assertEquals(savedBank.getUrl(), fetchedBank.getUrl());
    }


    @Test
    void failsToFetchIfBankCodeIsNullOrEmpty() {
        assertThrows(IllegalArgumentException.class, () -> bankInfoService.fetchBankByBankCode(null));
        assertThrows(IllegalArgumentException.class, () -> bankInfoService.fetchBankByBankCode(""));
    }

    @Test
    void failsToFetchBankIfBankCodeIsMissing() throws ResourceConflictException {
        assertEquals(null, bankInfoService.fetchBankByBankCode("ICIC"));
    }

    @Test
    void testGetAllBanks() throws ResourceConflictException {
        BankInfo bank1 = new BankInfo("HDFC1234", "hdfc-url");
        BankInfo bank2 = new BankInfo("AXIS1234", "axis-url");
        bankInfoService.create(bank1);
        bankInfoService.create(bank2);

        List<BankInfo> allBanks = bankInfoService.getAllBanks();

        assertEquals(2, allBanks.size());
    }
}
