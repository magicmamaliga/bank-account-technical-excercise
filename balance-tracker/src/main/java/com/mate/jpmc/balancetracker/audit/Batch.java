package com.mate.jpmc.balancetracker.audit;

import com.mate.jpmc.balancetracker.receiver.Transaction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Batch {

    public static final BigDecimal CAP = new BigDecimal(1_000_000);
    private BigDecimal remaining = CAP;
    private final List<Transaction> items = new ArrayList<>();

    public List<Transaction> getItems() {
        return items;
    }

    public void add(Transaction transaction) {
        BigDecimal value = transaction.amount().abs();
        if(value.compareTo(remaining) > 0) {
            throw new IllegalArgumentException("Value does not fit");
        }
        remaining = remaining.subtract(value);
        items.add(transaction);
    }


    public BigDecimal remaining() {
        return remaining;
    }

    @Override
    public String toString() {
        return "Batch{" + "remaining=" + remaining + ", items=" + items + '}';
    }
}
