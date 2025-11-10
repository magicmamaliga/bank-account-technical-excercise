package com.mate.jpmc.balancetracker.audit;

import com.mate.jpmc.balancetracker.receiver.TransactionDTO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Batch {

    public static final BigDecimal CAP = new BigDecimal(1_000_000);
    private final List<TransactionDTO> items = new ArrayList<>();
    private BigDecimal remaining = CAP;

    public List<TransactionDTO> getItems() {
        return items;
    }

    public void add(TransactionDTO transactionDTO) {
        BigDecimal value = transactionDTO.amount().abs();
        if (value.compareTo(remaining) > 0) {
            throw new IllegalArgumentException("Value does not fit");
        }
        remaining = remaining.subtract(value);
        items.add(transactionDTO);
    }


    public BigDecimal remaining() {
        return remaining;
    }

    @Override
    public String toString() {
        return "Batch{" + "remaining=" + remaining + ", items=" + items + '}';
    }
}
