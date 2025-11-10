package com.mate.jpmc.balancetracker.balance.cache;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Component
public class AccountBalanceCache {

    Map<String, BigDecimal> accountBalances = new ConcurrentHashMap<>();

    public BigDecimal getBalance(String accountId) {
        return accountBalances.get(accountId);
    }

    public void putBalance(String accountId, BigDecimal balance) {
        accountBalances.put(accountId, balance);
    }

    public boolean containsBalance(String accountId) {
        return accountBalances.containsKey(accountId);
    }

    public void computeIfAbsent(String accountId, Function<String, BigDecimal> function) {
        accountBalances.computeIfAbsent(accountId, function);
    }
}