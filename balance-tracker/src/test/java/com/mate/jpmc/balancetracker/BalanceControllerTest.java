package com.mate.jpmc.balancetracker;

import com.mate.jpmc.balancetracker.balance.BalanceController;
import com.mate.jpmc.balancetracker.balance.BankAccountServiceImpl;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BalanceController.class)
class BalanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BankAccountServiceImpl bankAccountService;

    @BeforeEach
    void resetMocks() {
        reset(bankAccountService);
    }

    @Test
    void getBalance_returnsOkJson_andInvokesService() throws Exception {
        when(bankAccountService.retrieveBalance()).thenReturn(new BigDecimal("123.45"));

        mockMvc.perform(get("/balance"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("123.45"));

        verify(bankAccountService, times(1)).retrieveBalance();
        verifyNoMoreInteractions(bankAccountService);
    }

    @Test
    void getBalance_includesCorsHeader_forAllowedOrigin() throws Exception {
        when(bankAccountService.retrieveBalance()).thenReturn(new BigDecimal("10"));

        mockMvc.perform(get("/balance")
                        .header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(content().string("10"));
    }

    @Test
    void getBalance_omitsCorsHeader_forDisallowedOrigin() throws Exception {

        mockMvc.perform(get("/balance")
                        .header("Origin", "http://evil.example.com"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"))
                .andExpect(content().string("Invalid CORS request"));
        verify(bankAccountService, times(0)).retrieveBalance();

    }

    @Test
    void preflightOptions_allowsGet_forAllowedOrigin() throws Exception {
        mockMvc.perform(options("/balance")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Methods",
                        Matchers.containsString("GET")));
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        BankAccountServiceImpl bankAccountService() {
            return Mockito.mock(BankAccountServiceImpl.class);
        }
    }
}
