package com.mate.jpmc.balancetracker;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;


public class Balance {

    private final AtomicReference<BigDecimal> balance = new AtomicReference<>(BigDecimal.ZERO);
    private final List<BigDecimal> balances = new ArrayList<>();

    public void deposit(BigDecimal amount) {
        balance.updateAndGet(b -> b.add(amount));
        balances.add(amount);
    }

    public BigDecimal getBalance() {
        return balance.get();
    }

}
