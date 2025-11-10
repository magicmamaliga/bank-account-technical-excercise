package com.mate.jpmc.balancetracker.receiver;

import java.math.BigDecimal;

public record TransactionDTO(String transactionId, String accountId, TransactionType transactionType,
                             BigDecimal amount) {
}
