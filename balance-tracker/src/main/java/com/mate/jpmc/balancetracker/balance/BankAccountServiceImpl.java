package com.mate.jpmc.balancetracker.balance;

import com.mate.jpmc.balancetracker.receiver.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class BankAccountServiceImpl implements  BankAccountService {

    private static final Logger log = LoggerFactory.getLogger(BankAccountServiceImpl.class);

    final Account account;

    public BankAccountServiceImpl(Account account) {
        this.account = account;
    }

    public BigDecimal retrieveBalance() {
        return  account.getBalance();
    }

    public void processTransaction(Transaction transaction) {
        account.deposit(transaction);
    }

}
