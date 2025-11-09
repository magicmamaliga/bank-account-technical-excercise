package com.mate.jpmc.balancetracker.balance.cache;

import com.mate.jpmc.balancetracker.receiver.TransactionDTO;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class AccountCache {


    private final AtomicReference<BigDecimal> balance = new AtomicReference<>(BigDecimal.ZERO);


    public void deposit(TransactionDTO transactionDTO) {
//        log.info("Entering deposit {}, auditQueue: {}", transactionDTO, auditQueue.size());
//        balance.updateAndGet(b -> b.add(transactionDTO.amount()));
//        auditQueue.add(transactionDTO);
    }

    public BigDecimal getBalance() {
        return balance.get();
    }
}
