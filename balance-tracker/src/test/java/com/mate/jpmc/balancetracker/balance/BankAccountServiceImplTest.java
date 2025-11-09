package com.mate.jpmc.balancetracker.balance;

import com.mate.jpmc.balancetracker.BalanceTrackerException;
import com.mate.jpmc.balancetracker.balance.model.Account;
import com.mate.jpmc.balancetracker.receiver.TransactionDTO;
import com.mate.jpmc.balancetracker.receiver.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankAccountServiceImplParameterizedTest {

    @Mock
    Account account;

    @InjectMocks
    BankAccountServiceImpl bankAccountService;

    @BeforeEach
    void setUp() {
        Mockito.reset(account);
    }

    @Test
    void retrieveBalance_delegatesToAccount() throws BalanceTrackerException {
        String accountId = "accountId";
        var result = bankAccountService.retrieveBalance(accountId);

        assertThat(result).isEqualByComparingTo("123.45");
        verifyNoMoreInteractions(account);
    }

    @ParameterizedTest
    @ValueSource(strings = {"200.01", "499999.99", "-250", "300000"})
    void processTransaction_validAmount_callsDeposit(String amountStr) throws BalanceTrackerException {
        var tx = new TransactionDTO(null,"id", TransactionType.CREDIT, new BigDecimal(amountStr));

        bankAccountService.processTransaction(tx);

        verifyNoMoreInteractions(account);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"200", "0", "-199.99", "500000", "600000", "-500000"})
    void processTransaction_invalidAmount_throwsException(String amountStr) {
        var amount = amountStr == null ? null : new BigDecimal(amountStr);
        var tx = new TransactionDTO(null, "id", TransactionType.CREDIT, amount);

        assertThatThrownBy(() -> bankAccountService.processTransaction(tx))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid amount");

        verifyNoMoreInteractions(account);
    }
}
