package com.mate.jpmc.balancetracker.audit;

import com.mate.jpmc.balancetracker.balance.model.Account;
import com.mate.jpmc.balancetracker.receiver.TransactionDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.BlockingQueue;

import static com.mate.jpmc.balancetracker.audit.Batch.CAP;

@Service
public class AuditBatcher implements SchedulingConfigurer {
//
//    private static final Logger LOG = LoggerFactory.getLogger(AuditBatcher.class);
////    final BlockingQueue<TransactionDTO> auditQueue;
////    final AuditSender auditSender;
//
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("AuditBatcher-");
        scheduler.initialize();
        return scheduler;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
//
//        taskRegistrar.setScheduler(taskScheduler());
//        taskRegistrar.addTriggerTask(this::submitForAudit,
//                triggerContext -> {
//            int backlog = auditQueue.size();
//            int delay = backlog > 10_000 ? 50 :
//                    backlog > 5_000 ? 100 : 1000;
//            LOG.info("Delay: {}", delay);
//            Instant base = triggerContext.lastCompletion();
//            if(base == null) {
//                base = Instant.now();
//            }
//            return base.plusMillis(delay);
//        });
//    }
//
//
//    @Value("${submission.size:1000}")   // set 1000 or 100000
//    private int submissionSize;
//
////    public AuditBatcher(Account account, AuditSender auditSender) {
////        this.auditQueue = account.getAuditQueue();
////        this.auditSender = auditSender;
////    }
//
////    @Scheduled(fixedDelayString = "${audit.poll.delay.ms:1000}")
//    public void submitForAudit() {
//        LOG.info("Entering trySubmission {}", auditQueue.size());
//
//        if (auditQueue.size() < submissionSize)
//            return;
//
//        int thousand = auditQueue.size() / submissionSize;
//        int drainedSize = thousand * submissionSize;
//
//        final List<TransactionDTO> window = new ArrayList<>(drainedSize);
//        auditQueue.drainTo(window, submissionSize);
//
//        LOG.info("Create batches");
//        Submission submission;
//        try {
//            submission = buildBatches(window);
//        } catch (AuditException e) {
//            for (TransactionDTO t : window)
//                auditQueue.offer(t);
//            return;
//        }
//        auditSender.sendSubmission(submission);
//    }
//
//
//    Submission buildBatches(List<TransactionDTO> transactionDTOS) throws AuditException {
//        if(transactionDTOS ==null || transactionDTOS.isEmpty() || transactionDTOS.size()< submissionSize){
//            LOG.info("The number of transactions is invalid {}", transactionDTOS);
//            throw new AuditException("The number of transactions must be greater than the minimum window size.");
//        }
//        List<TransactionDTO> sortedTransactionDTOS = new ArrayList<>(transactionDTOS);
//        sortedTransactionDTOS.sort((a, b) -> b.amount().abs().compareTo(a.amount().abs()));
//
//        TreeMap<BigDecimal, Deque<Batch>> remainingSpaceBatchMap = new TreeMap<>();
//
//        List<Batch> batches = new ArrayList<>();
//
//        for (TransactionDTO transactionDTO : sortedTransactionDTOS) {
//            BigDecimal amount = transactionDTO.amount().abs();
//
//            if (amount.compareTo(CAP) > 0) {
//                throw new IllegalArgumentException("Amount must be less than or equal to CAP");
//            }
//
//            Map.Entry<BigDecimal, Deque<Batch>> fit = remainingSpaceBatchMap.ceilingEntry(amount);
//            if (fit == null) {
//                Batch b = new Batch();
//                b.add(transactionDTO);
//                batches.add(b);
//                remainingSpaceBatchMap.computeIfAbsent(b.remaining(), k -> new ArrayDeque<>()).add(b);
//            } else {
//                Deque<Batch> q = fit.getValue();
//                Batch b = q.pollFirst();
//
//                if (q.isEmpty())
//                    remainingSpaceBatchMap.remove(fit.getKey());
//
//                b.add(transactionDTO);
//                remainingSpaceBatchMap.computeIfAbsent(b.remaining(), k -> new ArrayDeque<>()).add(b);
//            }
//        }
//        return new Submission(batches);
    }


}