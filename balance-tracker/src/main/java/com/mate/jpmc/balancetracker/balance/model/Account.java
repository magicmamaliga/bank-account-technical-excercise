package com.mate.jpmc.balancetracker.balance.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "accounts")
public record Account(
        @Id String id,
        String accountId,
        String name,
        String address
) {
}