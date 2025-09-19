package com.mate.jpmc.balancetracker.balance;

import java.math.BigDecimal;

public record BalanceRecord(String accountNumber, BigDecimal balance) {
}