package com.mate.jpmc.balancetracker;

import java.math.BigDecimal;

public record Transaction(String id, TransactionType transactionType, BigDecimal amount) {}
