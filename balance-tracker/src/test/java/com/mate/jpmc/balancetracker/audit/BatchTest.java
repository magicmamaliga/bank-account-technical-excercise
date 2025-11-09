package com.mate.jpmc.balancetracker.audit;

import com.mate.jpmc.balancetracker.receiver.TransactionDTO;
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
        TransactionDTO tx = new TransactionDTO("id1", TransactionType.CREDIT, new BigDecimal("200000"));

        batch.add(tx);

        assertThat(batch.getItems()).containsExactly(tx);
        assertThat(batch.remaining()).isEqualByComparingTo("800000");
    }

    @Test
    void addTransactionExceedingRemaining_throwsException() {
        TransactionDTO tx1 = new TransactionDTO("id1", TransactionType.CREDIT, new BigDecimal("900000"));
        TransactionDTO tx2 = new TransactionDTO("id2", TransactionType.CREDIT, new BigDecimal("200000"));

        batch.add(tx1);

        assertThrows(IllegalArgumentException.class, () -> batch.add(tx2));
        assertThat(batch.getItems()).containsExactly(tx1);
        assertThat(batch.remaining()).isEqualByComparingTo("100000"); // unchanged
    }

    @Test
    void addMultipleTransactions_accumulatesUntilCap() {
        TransactionDTO tx1 = new TransactionDTO("id1", TransactionType.CREDIT, new BigDecimal("-400000"));
        TransactionDTO tx2 = new TransactionDTO("id2", TransactionType.DEBIT, new BigDecimal("-300000"));
        TransactionDTO tx3 = new TransactionDTO("id3", TransactionType.CREDIT, new BigDecimal("300000"));

        batch.add(tx1);
        batch.add(tx2);
        batch.add(tx3);

        assertThat(batch.getItems()).containsExactly(tx1, tx2, tx3);
        assertThat(batch.remaining()).isEqualByComparingTo("0");
    }

    @Test
    void addTransactionEqualToRemaining_fitsExactly() {
        TransactionDTO tx = new TransactionDTO("idX", TransactionType.CREDIT, Batch.CAP);

        batch.add(tx);

        assertThat(batch.remaining()).isEqualByComparingTo("0");
        assertThat(batch.getItems()).containsExactly(tx);
    }

    @Test
    void addTransactionAboveCap_throwsException() {
        TransactionDTO tooLarge = new TransactionDTO("idBig", TransactionType.CREDIT, new BigDecimal("1000001"));

        assertThrows(IllegalArgumentException.class, () -> batch.add(tooLarge));

        assertThat(batch.getItems()).isEmpty();
        assertThat(batch.remaining()).isEqualByComparingTo(Batch.CAP); // still full
    }
}
