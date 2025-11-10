package com.mate.jpmc.balancetracker.balance;

import com.mate.jpmc.balancetracker.BalanceTrackerException;
import com.mate.jpmc.balancetracker.balance.cache.AccountBalanceCache;
import com.mate.jpmc.balancetracker.balance.model.Account;
import com.mate.jpmc.balancetracker.balance.model.AccountRepository;
import com.mate.jpmc.balancetracker.balance.model.Transaction;
import com.mate.jpmc.balancetracker.balance.model.TransactionRepository;
import com.mate.jpmc.balancetracker.receiver.TransactionDTO;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class BankAccountServiceImpl implements  BankAccountService {

    @Resource
    AccountRepository accountRepository;

    @Resource
    TransactionRepository transactionRepository;

    @Resource
    AccountBalanceCache accountBalanceCache;

    public BigDecimal retrieveBalance(String accountId) throws BalanceTrackerException {
        log.info("Retrieving balance for account {}", accountId);

        if (StringUtils.hasText(accountId)) {
            throw new BalanceTrackerException("Account Id can't be null or empty");
        }

        Optional<Account> account = accountRepository.findByAccountId(accountId);
        if (account.isEmpty()) {
            throw new BalanceTrackerException("Account not found");
        }

        return calculateBalance(accountId);
    }

    private BigDecimal calculateBalance(String accountId) throws BalanceTrackerException {
        if (StringUtils.hasText(accountId)) {
            throw new BalanceTrackerException("Account Id can't be null or empty");
        }
        List<Transaction> transactions = transactionRepository.findByAccountId(accountId);

        return transactions.stream().map(Transaction::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Counted(value = "processTransactionCount", description = "processTransaction")
    @Timed(value = "processTransactionTimed", description = "processTransaction")
    public void processTransaction(TransactionDTO transactionDTO) throws BalanceTrackerException {
        log.info("Processing transaction {}", transactionDTO);

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

        BigDecimal accountBalanceCacheBalance = accountBalanceCache.getBalance(transactionDTO.accountId());
        if (accountBalanceCacheBalance == null) {
            Optional<Account> account = accountRepository.findByAccountId(transactionDTO.accountId());
            if (account.isEmpty()) {
                log.info("Account not found accountId {}", transactionDTO.accountId());
                throw new BalanceTrackerException("Account not found");
            }
            accountBalanceCache.putBalance(transactionDTO.accountId(), BigDecimal.ZERO);
        }

        Transaction transaction = new Transaction(null,
                transactionDTO.transactionId(),
                transactionDTO.accountId(),
                transactionDTO.transactionType(),
                transactionDTO.amount(),
                new Date());

        transactionRepository.save(transaction);
    }

}
