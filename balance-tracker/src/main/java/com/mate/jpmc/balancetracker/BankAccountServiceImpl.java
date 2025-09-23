package com.mate.jpmc.balancetracker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class BankAccountServiceImpl implements  BankAccountService{

    private static final Logger log = LoggerFactory.getLogger(BankAccountServiceImpl.class);

    Balance balance = new Balance();

    public BigDecimal retrieveBalance() {
        log.info("Retrieving balance {}", balance.getBalance());
        return  balance.getBalance();
    }

    public void processTransaction(Transaction transaction) {
        log.info("Processing Transaction {}", transaction);
        balance.deposit(BigDecimal.valueOf(transaction.amount()));
    }
}
