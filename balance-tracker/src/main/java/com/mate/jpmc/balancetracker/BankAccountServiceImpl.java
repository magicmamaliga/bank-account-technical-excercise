package com.mate.jpmc.balancetracker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class BankAccountServiceImpl implements  BankAccountService {

    private static final Logger log = LoggerFactory.getLogger(BankAccountServiceImpl.class);

    final Account account;

    public BankAccountServiceImpl(Account account) {
        this.account = account;
    }

    public BigDecimal retrieveBalance() {
        log.info("Retrieving balance {}", account.getBalance());
        return  account.getBalance();
    }

    public void processTransaction(Transaction transaction) {
//        log.info("Processing Transaction {}", transaction);
        account.deposit(transaction);
    }

}
