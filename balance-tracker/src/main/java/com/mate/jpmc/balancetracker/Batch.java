package com.mate.jpmc.balancetracker;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Batch {

    public static final BigDecimal CAP = new BigDecimal(1_000_000);
    private BigDecimal remaining = CAP;
    private BigDecimal total = BigDecimal.ZERO;
    private List<Transaction> items = new ArrayList<>();

    public BigDecimal getRemaining() {
        return remaining;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public List<Transaction> getItems() {
        return items;
    }

    void add(Transaction transaction) {
        BigDecimal value = transaction.amount().abs();
        if(value.compareTo(remaining) > 0) {
            throw new IllegalArgumentException("Value does not fit");
        }
        remaining = remaining.subtract(value);
        total = total.add(value);
        items.add(transaction);
    }


    public BigDecimal remaining() {
        return remaining;
    }

    @Override
    public String toString() {
        return "Batch{" +
                "remaining=" + remaining +
                ", total=" + total +
                ", items=" + items +
                '}';
    }
}
