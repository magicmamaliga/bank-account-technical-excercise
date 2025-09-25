package com.mate.jpmc.balancetracker.audit;

import com.mate.jpmc.balancetracker.balance.Account;
import com.mate.jpmc.balancetracker.receiver.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.BlockingQueue;

import static com.mate.jpmc.balancetracker.audit.Batch.CAP;

@Service
public class AuditBatcher {

    private static final Logger LOG = LoggerFactory.getLogger(AuditBatcher.class);
    final BlockingQueue<Transaction> auditQueue;
    final AuditSender auditSender;

    @Value("${audit.minimum.window.size:1000}")   // set 1000 or 100000
    private int minimumWindowSize;

    public AuditBatcher(Account account, AuditSender auditSender) {
        this.auditQueue = account.getAuditQueue();
        this.auditSender = auditSender;
    }

    @Scheduled(fixedDelayString = "${audit.poll.delay.ms:1000}")
    public void trySubmission() {
        LOG.info("Entering trySubmission {}", auditQueue.size());

        if (auditQueue.size() < minimumWindowSize)
            return;

        final List<Transaction> window = new ArrayList<>(minimumWindowSize);
        auditQueue.drainTo(window, minimumWindowSize);

        //in case something goes wrong during drain
        if (window.size() < minimumWindowSize) {
            for (Transaction t : window)
                auditQueue.offer(t);
            return;
        }

        LOG.info("Create batches");
        Submission submission;
        try {
            submission = buildBatches(window);
        } catch (AuditException e) {
            for (Transaction t : window)
                auditQueue.offer(t);
            return;
        }
        auditSender.sendSubmission(submission);
    }


    Submission buildBatches(List<Transaction> transactions) throws AuditException {
        if(transactions==null || transactions.isEmpty() || transactions.size()<minimumWindowSize ){
            LOG.info("The number of transactions is invalid {}", transactions);
            throw new AuditException("The number of transactions must be greater than the minimum window size.");
        }
        List<Transaction> sortedTransactions = new ArrayList<>(transactions);
        sortedTransactions.sort((a, b) -> b.amount().abs().compareTo(a.amount().abs()));

        TreeMap<BigDecimal, Deque<Batch>> remainingSpaceBatchMap = new TreeMap<>();

        List<Batch> batches = new ArrayList<>();

        for (Transaction transaction : sortedTransactions) {
            BigDecimal amount = transaction.amount().abs();

            if (amount.compareTo(CAP) > 0) {
                throw new IllegalArgumentException("Amount must be less than or equal to CAP");
            }

            Map.Entry<BigDecimal, Deque<Batch>> fit = remainingSpaceBatchMap.ceilingEntry(amount);
            if (fit == null) {
                Batch b = new Batch();
                b.add(transaction);
                batches.add(b);
                remainingSpaceBatchMap.computeIfAbsent(b.remaining(), k -> new ArrayDeque<>()).add(b);
            } else {
                Deque<Batch> q = fit.getValue();
                Batch b = q.pollFirst();

                if (q.isEmpty())
                    remainingSpaceBatchMap.remove(fit.getKey());

                b.add(transaction);
                remainingSpaceBatchMap.computeIfAbsent(b.remaining(), k -> new ArrayDeque<>()).add(b);
            }
        }
        return new Submission(batches);
    }

}