package com.mate.jpmc.balancetracker.balance.model;

import com.mate.jpmc.balancetracker.receiver.TransactionType;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.Date;

@Document(collection = "transactions")
public record Transaction(@Id String id, String transactionId, String accountId, TransactionType transactionType, BigDecimal amount, Date date) {
}
