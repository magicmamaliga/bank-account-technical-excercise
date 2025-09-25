package com.mate.jpmc.balancetracker.receiver;

import java.math.BigDecimal;

public record Transaction(String id, TransactionType transactionType, BigDecimal amount) {}
