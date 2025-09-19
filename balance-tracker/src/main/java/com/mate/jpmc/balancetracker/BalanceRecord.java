package com.mate.jpmc.balancetracker;

import java.math.BigDecimal;

public record BalanceRecord(String accountNumber, BigDecimal balance) {
}