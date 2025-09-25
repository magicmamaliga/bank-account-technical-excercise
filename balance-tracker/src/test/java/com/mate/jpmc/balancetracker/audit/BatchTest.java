package com.mate.jpmc.balancetracker.audit;

import com.mate.jpmc.balancetracker.receiver.Transaction;
import com.mate.jpmc.balancetracker.receiver.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BatchTest {

    private Batch batch;

    @BeforeEach
    void setUp() {
        batch = new Batch();
    }

    @Test
    void addTransactionWithinCap_updatesRemainingAndItems() {
        Transaction tx = new Transaction("id1", TransactionType.CREDIT, new BigDecimal("200000"));

        batch.add(tx);

        assertThat(batch.getItems()).containsExactly(tx);
        assertThat(batch.remaining()).isEqualByComparingTo("800000");
    }

    @Test
    void addTransactionExceedingRemaining_throwsException() {
        Transaction tx1 = new Transaction("id1", TransactionType.CREDIT, new BigDecimal("900000"));
        Transaction tx2 = new Transaction("id2", TransactionType.CREDIT, new BigDecimal("200000"));

        batch.add(tx1);

        assertThrows(IllegalArgumentException.class, () -> batch.add(tx2));
        assertThat(batch.getItems()).containsExactly(tx1);
        assertThat(batch.remaining()).isEqualByComparingTo("100000"); // unchanged
    }

    @Test
    void addMultipleTransactions_accumulatesUntilCap() {
        Transaction tx1 = new Transaction("id1", TransactionType.CREDIT, new BigDecimal("-400000"));
        Transaction tx2 = new Transaction("id2", TransactionType.DEBIT, new BigDecimal("-300000"));
        Transaction tx3 = new Transaction("id3", TransactionType.CREDIT, new BigDecimal("300000"));

        batch.add(tx1);
        batch.add(tx2);
        batch.add(tx3);

        assertThat(batch.getItems()).containsExactly(tx1, tx2, tx3);
        assertThat(batch.remaining()).isEqualByComparingTo("0");
    }

    @Test
    void addTransactionEqualToRemaining_fitsExactly() {
        Transaction tx = new Transaction("idX", TransactionType.CREDIT, Batch.CAP);

        batch.add(tx);

        assertThat(batch.remaining()).isEqualByComparingTo("0");
        assertThat(batch.getItems()).containsExactly(tx);
    }

    @Test
    void addTransactionAboveCap_throwsException() {
        Transaction tooLarge = new Transaction("idBig", TransactionType.CREDIT, new BigDecimal("1000001"));

        assertThrows(IllegalArgumentException.class, () -> batch.add(tooLarge));

        assertThat(batch.getItems()).isEmpty();
        assertThat(batch.remaining()).isEqualByComparingTo(Batch.CAP); // still full
    }
}
