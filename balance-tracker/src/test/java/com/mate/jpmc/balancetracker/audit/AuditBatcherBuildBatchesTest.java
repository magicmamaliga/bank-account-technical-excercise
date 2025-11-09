package com.mate.jpmc.balancetracker.audit;

import com.mate.jpmc.balancetracker.balance.model.Account;
import com.mate.jpmc.balancetracker.receiver.TransactionDTO;
import com.mate.jpmc.balancetracker.receiver.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

import static com.mate.jpmc.balancetracker.audit.Batch.CAP;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for AuditBatcher#buildBatches(List<Transaction>).
 */
class AuditBatcherBuildBatchesTest {

    private AuditBatcher batcher;
    private Account account;
    private AuditSender sender;

    private static TransactionDTO credit(BigDecimal absAmount) {
        if (absAmount.signum() < 0) absAmount = absAmount.abs();
        return new TransactionDTO(UUID.randomUUID().toString(), TransactionType.CREDIT, absAmount);
    }

    private static TransactionDTO debit(BigDecimal absAmount) {
        if (absAmount.signum() < 0) absAmount = absAmount.abs();
        return new TransactionDTO(UUID.randomUUID().toString(), TransactionType.DEBIT, absAmount.negate());
    }

    /**
     * Tiny builder for positive credits (integers).
     */
    private static List<TransactionDTO> creditsOf(int... ints) {
        List<TransactionDTO> out = new ArrayList<>(ints.length);
        for (int i : ints) out.add(credit(BigDecimal.valueOf(i)));
        return out;
    }

    private static void assertAllBatchesWithinCapAndAllItemsAccountedFor(List<Batch> batches, int expectedTotalItems) {
        int counted = 0;
        for (Batch b : batches) {
            BigDecimal sumAbs = b.getItems().stream()
                    .map(t -> t.amount().abs())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            assertTrue(sumAbs.compareTo(CAP) <= 0, "Batch exceeds CAP: " + sumAbs);
            counted += b.getItems().size();
        }
        assertEquals(expectedTotalItems, counted, "All transactions should be assigned to some batch");
    }

    private static void setSubmissionSizePrivateInt(Object target, int value) throws Exception {
        Field f = target.getClass().getDeclaredField("submissionSize");
        f.setAccessible(true);
        f.setInt(target, value);
    }

    @BeforeEach
    void setUp() throws Exception {
        account = mock(Account.class);
        when(account.getAuditQueue()).thenReturn(new LinkedBlockingQueue<>());

        sender = mock(AuditSender.class);

        batcher = new AuditBatcher(account, sender);

        setSubmissionSizePrivateInt(batcher, 1000);
    }

    // ------------------------------------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------------------------------------

    @Test
    void rejectsNullEmptyOrTooSmall() throws Exception {
        setSubmissionSizePrivateInt(batcher, 5);

        assertThrows(AuditException.class, () -> batcher.buildBatches(null));
        assertThrows(AuditException.class, () -> batcher.buildBatches(List.of()));

        List<TransactionDTO> four = creditsOf(1, 2, 3, 4);
        assertThrows(AuditException.class, () -> batcher.buildBatches(four));
    }

    @Test
    void throwsIfSingleTransactionExceedsCap() throws Exception {
        setSubmissionSizePrivateInt(batcher, 1);

        TransactionDTO overCredit = credit(CAP.add(BigDecimal.ONE));
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class,
                () -> batcher.buildBatches(List.of(overCredit)));
        assertTrue(ex1.getMessage().toLowerCase().contains("cap"));

        TransactionDTO overDebit = debit(CAP.add(BigDecimal.ONE));
        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class,
                () -> batcher.buildBatches(List.of(overDebit)));
        assertTrue(ex2.getMessage().toLowerCase().contains("cap"));
    }

    @Test
    void creditsDontOffsetDebits_andNoBatchExceedsCap() throws Exception {
        setSubmissionSizePrivateInt(batcher, 6);

        BigDecimal sixHundredK = new BigDecimal("600000");
        BigDecimal fourHundredK = new BigDecimal("400000");

        List<TransactionDTO> window = List.of(
                credit(sixHundredK),  // +600k
                debit(sixHundredK),   // -600k
                credit(fourHundredK), // +400k
                debit(fourHundredK),  // -400k
                credit(fourHundredK), // +400k
                debit(fourHundredK)   // -400k
        );

        Submission s = batcher.buildBatches(window);
        List<Batch> batches = s.batches();

        // For this set: optimal packing is 3 batches (600+400), (600+400), (400+400)
        assertTrue(batches.size() <= 3, "Packing should not exceed 3 batches for this set");

        // No batch exceeds CAP by absolute sum, and all transactions are included once
        assertAllBatchesWithinCapAndAllItemsAccountedFor(batches, window.size());
    }

    @Test
    void greedyFirstFitDecreasingPacksReasonably_random100() throws Exception {
        setSubmissionSizePrivateInt(batcher, 100);

        // 100 random transactions in [200, 500_000], random sign
        Random r = new Random(42);
        List<TransactionDTO> window = new ArrayList<>(100);
        for (int i = 0; i < 100; i++) {
            int v = 200 + r.nextInt(500_000 - 200 + 1);
            window.add(r.nextBoolean() ? credit(BigDecimal.valueOf(v)) : debit(BigDecimal.valueOf(v)));
        }

        Submission s = batcher.buildBatches(window);
        List<Batch> batches = s.batches();

        assertAllBatchesWithinCapAndAllItemsAccountedFor(batches, window.size());
        assertTrue(batches.size() <= window.size(), "Greedy should not use more batches than items");
    }

    @Test
    void exactFillToCapCreatesSingleBatch() throws Exception {
        setSubmissionSizePrivateInt(batcher, 3);

        // 700k + 200k + 100k = 1_000_000 exactly
        List<TransactionDTO> window = List.of(
                credit(new BigDecimal("700000")),
                debit(new BigDecimal("200000")),
                credit(new BigDecimal("100000"))
        );

        Submission s = batcher.buildBatches(window);
        List<Batch> batches = s.batches();

        assertEquals(1, batches.size(), "Should fit into a single batch exactly at CAP");
        BigDecimal remaining = batches.getFirst().remaining();
        assertEquals(BigDecimal.ZERO, remaining, "Remaining should be 0 when exactly filled");
    }
}
