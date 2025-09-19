package com.mate.jpmc.balancetracker;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class BankAccountService {

    public BalanceRecord getBalance() {
        return new BalanceRecord("ACC-1234567", new BigDecimal(1));
    }

}
