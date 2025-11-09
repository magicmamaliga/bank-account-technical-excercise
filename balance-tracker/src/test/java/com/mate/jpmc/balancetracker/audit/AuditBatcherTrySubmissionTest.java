package com.mate.jpmc.balancetracker.audit;

import com.mate.jpmc.balancetracker.balance.model.Account;
import com.mate.jpmc.balancetracker.receiver.TransactionDTO;
import com.mate.jpmc.balancetracker.receiver.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Tests for AuditBatcher#trySubmission()
 * <p>
 * Scenarios:
 * - queue size < minimumWindowSize -> early return, nothing sent
 * - happy path -> drains exactly minimumWindowSize, calls buildBatches, then sendSubmission
 * - queue > minimumWindowSize -> drains only minimumWindowSize, leaves remainder
 * - buildBatches throws -> items rolled back to queue, nothing sent
 * - partial drain (drained < minimumWindowSize) -> items rolled back to queue, nothing sent
 */
class AuditBatcherTrySubmissionTest {

    private AuditBatcher batcher;       // we'll spy this
    private Account account;
    private AuditSender sender;
    private BlockingQueue<TransactionDTO> auditQueue;

    private static void offerMany(BlockingQueue<TransactionDTO> q, int count) {
        for (TransactionDTO t : makeTransactions(count)) q.offer(t);
    }

    private static List<TransactionDTO> makeTransactions(int count) {
        List<TransactionDTO> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            // alternate credit/debit; values in [200..500_000]
            BigDecimal abs = BigDecimal.valueOf(200L + (i % 500_000));
            TransactionDTO tx = (i % 2 == 0)
                    ? new TransactionDTO(UUID.randomUUID().toString(), TransactionType.CREDIT, abs)
                    : new TransactionDTO(UUID.randomUUID().toString(), TransactionType.DEBIT, abs.negate());
            list.add(tx);
        }
        return list;
    }

    private static void setPrivateInt(Object target, String fieldName, int value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.setInt(target, value);
    }

    @BeforeEach
    void setUp() throws Exception {
        account = mock(Account.class);
        sender = mock(AuditSender.class);
        auditQueue = new LinkedBlockingQueue<>();
        when(account.getAuditQueue()).thenReturn(auditQueue);

        // real instance so the queue reference is wired; then spy to stub buildBatches
        AuditBatcher real = new AuditBatcher(account, sender);
        setPrivateInt(real, "submissionSize", 10); // default N=10; override per-test if desired
        batcher = spy(real);
    }

    @Test
    void whenQueueHasLessThanWindow_thenEarlyReturn_NoSend() throws AuditException {
        // queue size 7 < N(10)
        offerMany(auditQueue, 7);

        batcher.submitForAudit();

        // nothing sent
        verify(sender, never()).sendSubmission(any());
        // items remain untouched
        assertEquals(7, auditQueue.size());
        // buildBatches never invoked
        verify(batcher, never()).buildBatches(anyList());
    }

    @Test
    void happyPath_exactWindowSize_drainsAndSends() throws Exception {
        // exactly N
        int N = 10;
        setPrivateInt(batcher, "submissionSize", N);
        offerMany(auditQueue, N);

        // stub buildBatches -> returns a dummy submission
        Submission dummy = new Submission(List.of());
        doReturn(dummy).when(batcher).buildBatches(anyList());

        batcher.submitForAudit();

        // drained all N
        assertEquals(0, auditQueue.size());
        // build then send
        InOrder inOrder = inOrder(batcher, sender);
        inOrder.verify(batcher).buildBatches(argThat(list -> list.size() == N));
        inOrder.verify(sender).sendSubmission(dummy);
        verifyNoMoreInteractions(sender);
    }

    // ------------------------------------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------------------------------------

    @Test
    void happyPath_moreThanWindowSize_drainsOnlyN_leavesRemainder() throws Exception {
        int N = 10;
        setPrivateInt(batcher, "submissionSize", N);
        offerMany(auditQueue, 13); // 23 in queue

        Submission dummy = new Submission(List.of());
        doReturn(dummy).when(batcher).buildBatches(anyList());

        batcher.submitForAudit();

        // should drain only N, leaving 13
        assertEquals(3, auditQueue.size());
        verify(batcher).buildBatches(argThat(list -> list.size() == N));
        verify(sender).sendSubmission(dummy);
    }

    @Test
    void buildBatchesThrows_rollbackAllItems_NoSend() throws Exception {
        int N = 10;
        setPrivateInt(batcher, "submissionSize", N);
        offerMany(auditQueue, N);

        doThrow(new AuditException("boom")).when(batcher).buildBatches(anyList());

        batcher.submitForAudit();

        // all items put back
        assertEquals(N, auditQueue.size(), "All drained items must be returned on failure");
        verify(sender, never()).sendSubmission(any());
    }

    @Test
    void partialDrainDetected_rollBack_NoSend() throws Exception {
        int N = 10;
        setPrivateInt(batcher, "submissionSize", N);

        // Use a custom queue whose drainTo(Collection, N) drains only N-1 even if size>=N
        PartialDrainQueue partial = new PartialDrainQueue();
        when(account.getAuditQueue()).thenReturn(partial);
        AuditBatcher real = new AuditBatcher(account, sender);
        setPrivateInt(real, "submissionSize", N);
        AuditBatcher spied = spy(real);

        partial.offerAll(makeTransactions(N)); // size is N

        spied.submitForAudit();

        // Since window.size() < N, items must be put back; queue should end with original N
        assertEquals(N, partial.size(), "Items must be rolled back on partial drain");
        verify(spied, never()).buildBatches(anyList());
        verify(sender, never()).sendSubmission(any());
    }

    private static class PartialDrainQueue extends LinkedBlockingQueue<TransactionDTO> {
        @Override
        public int drainTo(java.util.Collection<? super TransactionDTO> c, int maxElements) {
            // drain one-less than requested if possible, simulating a partial drain glitch
            int toDrain = Math.min(size(), Math.max(0, maxElements - 1));
            int drained = 0;
            for (; drained < toDrain; drained++) {
                TransactionDTO t = poll();
                if (t == null) break;
                c.add(t);
            }
            return drained;
        }

        void offerAll(List<TransactionDTO> list) {
            for (TransactionDTO t : list) offer(t);
        }
    }
}
