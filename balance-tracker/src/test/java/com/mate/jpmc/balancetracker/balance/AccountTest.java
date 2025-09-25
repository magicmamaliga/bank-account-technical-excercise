package com.mate.jpmc.balancetracker.balance;

import com.mate.jpmc.balancetracker.receiver.Transaction;
import com.mate.jpmc.balancetracker.receiver.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class AccountTest {

    private Account account;

    @BeforeEach
    void setUp() {
        account = new Account();
    }

    @Test
    void initialBalance_shouldBeZero() {
        assertThat(account.getBalance()).isEqualByComparingTo("0");
        assertThat(account.getAuditQueue()).isEmpty();
    }

    @Test
    void deposit_creditIncreasesBalance_andQueuesTransaction() {
        Transaction tx = new Transaction("id-1", TransactionType.CREDIT, new BigDecimal("100.50"));

        account.deposit(tx);

        assertThat(account.getBalance()).isEqualByComparingTo("100.50");
        assertThat(account.getAuditQueue()).containsExactly(tx);
    }

    @Test
    void deposit_debitDecreasesBalance_andQueuesTransaction() {
        Transaction tx = new Transaction("id-2", TransactionType.DEBIT, new BigDecimal("-75"));

        account.deposit(tx);

        assertThat(account.getBalance()).isEqualByComparingTo("-75");
        assertThat(account.getAuditQueue()).containsExactly(tx);
    }

    @Test
    void multipleDeposits_accumulateBalance_andQueue() {
        Transaction tx1 = new Transaction("id-1", TransactionType.CREDIT, new BigDecimal("200"));
        Transaction tx2 = new Transaction("id-2", TransactionType.DEBIT, new BigDecimal("-50"));

        account.deposit(tx1);
        account.deposit(tx2);

        assertThat(account.getBalance()).isEqualByComparingTo("150");
        assertThat(account.getAuditQueue()).containsExactly(tx1, tx2);
    }

}
