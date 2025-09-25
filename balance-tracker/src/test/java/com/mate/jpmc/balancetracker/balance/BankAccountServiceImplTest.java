package com.mate.jpmc.balancetracker.balance;

import com.mate.jpmc.balancetracker.receiver.Transaction;
import com.mate.jpmc.balancetracker.receiver.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankAccountServiceImplTest {

    @Mock
    Account account;
    @InjectMocks
    BankAccountServiceImpl bankAccountService;

    @BeforeEach
    void setUp() {
        Mockito.reset(account);
    }

    @Test
    void retrieveBalance_delegatesToAccount() {
        when(account.getBalance()).thenReturn(new BigDecimal("123.45"));

        var result = bankAccountService.retrieveBalance();

        assertThat(result).isEqualByComparingTo("123.45");
        verify(account, times(1)).getBalance();
        verifyNoMoreInteractions(account);
    }

    @Test
    void processTransaction_callsDeposit() {
        var tx = new Transaction("id", TransactionType.CREDIT, new BigDecimal("10")
        );

        bankAccountService.processTransaction(tx);

        verify(account).deposit(tx);
        verifyNoMoreInteractions(account);
    }
}
