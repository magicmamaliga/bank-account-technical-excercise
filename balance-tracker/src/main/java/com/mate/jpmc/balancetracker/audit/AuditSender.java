package com.mate.jpmc.balancetracker.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuditSender {
    private static final Logger LOG = LoggerFactory.getLogger(AuditSender.class.getName());

    void sendSubmission(Submission submission) {
        LOG.info("Submission: batches count:{}, sizes:{}", submission.batches().size(),
                submission.batches().stream().map(x -> x.getItems().size()).mapToInt(Integer::intValue).sum());
    }
}
