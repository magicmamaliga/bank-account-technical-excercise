package com.mate.jpmc.balancetracker.balance;

import com.mate.jpmc.balancetracker.BalanceTrackerException;
import com.mate.jpmc.balancetracker.balance.cache.AccountBalanceCache;
import com.mate.jpmc.balancetracker.balance.cache.TransactionCache;
import com.mate.jpmc.balancetracker.balance.model.Account;
import com.mate.jpmc.balancetracker.balance.model.AccountRepository;
import com.mate.jpmc.balancetracker.balance.model.Transaction;
import com.mate.jpmc.balancetracker.balance.model.TransactionRepository;
import com.mate.jpmc.balancetracker.receiver.TransactionDTO;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

@Service
@Slf4j
public class BankAccountServiceImpl implements  BankAccountService {

    @Resource
    AccountRepository accountRepository;

    @Resource
    TransactionRepository transactionRepository;

    @Resource
    AccountBalanceCache accountBalanceCache;

    @Resource
    TransactionCache transactionCache;

    public BigDecimal retrieveBalance(String accountId) throws BalanceTrackerException {
        log.info("Retrieving balance for account {}", accountId);

        if (!StringUtils.hasText(accountId)) {
            throw new BalanceTrackerException("Account Id can't be null or empty");
        }

        Optional<Account> account = accountRepository.findByAccountId(accountId);
        if (account.isEmpty()) {
            throw new BalanceTrackerException("Account not found");
        }

        return calculateBalance(accountId);
    }

    private BigDecimal calculateBalance(String accountId) throws BalanceTrackerException {
        if (!StringUtils.hasText(accountId)) {
            throw new BalanceTrackerException("Account Id can't be null or empty");
        }
        List<Transaction> transactions = transactionRepository.findByAccountId(accountId);

        return transactions.stream().map(Transaction::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Counted(value = "processTransactionCount", description = "processTransaction")
    @Timed(value = "processTransactionTimed", description = "processTransaction")
    public void processTransaction(TransactionDTO transactionDTO) throws BalanceTrackerException {
//        log.info("Processing transaction {}", transactionDTO);

        BigDecimal amount = transactionDTO.amount();
        if (amount == null
                || amount.abs().compareTo(BigDecimal.valueOf(200)) <= 0
                || amount.abs().compareTo(BigDecimal.valueOf(500000)) >= 0) {
            log.info("Invalid transaction amount {}", amount);
            throw new BalanceTrackerException("Invalid amount: must be greater than 200 and less than 500000");
        }

        if (transactionDTO.accountId() == null || transactionDTO.accountId().isEmpty()) {
            log.info("Account Id can't be null or empty transaction {}", transactionDTO);
            throw new BalanceTrackerException("Account Id can't be null or empty");
        }

        accountBalanceCache.computeIfAbsent(transactionDTO.accountId(), (id)->{
            Optional<Account> account = accountRepository.findByAccountId(id);
            if (account.isEmpty()) {
                log.info("Account not found accountId {}", id);
                throw new RuntimeException("Account not found");
            }
            return BigDecimal.ZERO;
        });

        Transaction transaction = new Transaction(null,
                transactionDTO.transactionId(),
                transactionDTO.accountId(),
                transactionDTO.transactionType(),
                transactionDTO.amount(),
                new Date());
        transactions.offer(transaction);
        transactionCache.getTransactionCache().offer(transactionDTO);
        saveTransactions();
    }

    private void saveTransactions() {
        if (transactions.size() < 5000) {
            return;
        }

        List<Transaction> transactionBatch = new ArrayList<>(5000);

        transactions.drainTo(transactionBatch, 5000);
        batchExecutor.submit(() -> {
            transactionRepository.saveAll(transactionBatch);
            lastFlush = System.currentTimeMillis();
        });
    }

    ExecutorService batchExecutor =  Executors.newSingleThreadExecutor();
    BlockingDeque<Transaction> transactions = new LinkedBlockingDeque<>();
    private Long lastFlush;

    @Scheduled(fixedRate = 6000)
    void handleLeftover() {
        if (lastFlush == null) {
            return;
        }
        if (System.currentTimeMillis() - lastFlush > 6000 && !transactions.isEmpty()) {
            int size = transactions.size();
            List<Transaction> transactionBatch = new ArrayList<>(size);
            transactions.drainTo(transactionBatch, size);
            transactionRepository.saveAll(transactionBatch);
            lastFlush = System.currentTimeMillis();
        }

    }

}
