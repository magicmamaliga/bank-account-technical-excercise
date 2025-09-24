package com.mate.jpmc.balancetracker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class Account {
    private static final Logger LOG = LoggerFactory.getLogger(Account.class.getName());
    private final AtomicReference<BigDecimal> balance = new AtomicReference<>(BigDecimal.ZERO);
    BlockingQueue<Transaction> auditQueue = new LinkedBlockingQueue<>(120000);

    public void deposit(Transaction transaction) {
//        LOG.info("Entering deposit {}, auditQueue: {}", transaction, auditQueue.size());
        balance.updateAndGet(b -> b.add(transaction.amount()));
        auditQueue.add(transaction);
    }

    public BigDecimal getBalance() {
        return balance.get();
    }

}
