package com.mate.jpmc.balancetracker.balance.cache;

import com.mate.jpmc.balancetracker.receiver.TransactionDTO;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

@Component
@Getter
public class TransactionCache {

    private final BlockingDeque<TransactionDTO> transactionCache = new LinkedBlockingDeque<>();

}
