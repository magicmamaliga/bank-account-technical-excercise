package com.mate.jpmc.balancetracker.balance;

import com.mate.jpmc.balancetracker.transaction.Transaction;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class BankAccountService {

    public BalanceRecord getBalance() {
        return new BalanceRecord("ACC-1234567", new BigDecimal(1));
    }

    public void processTransaction(Transaction transaction) {

    }
}
