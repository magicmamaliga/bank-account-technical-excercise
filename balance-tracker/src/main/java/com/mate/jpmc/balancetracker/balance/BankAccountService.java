package com.mate.jpmc.balancetracker.balance;

import com.mate.jpmc.balancetracker.BalanceTrackerException;
import com.mate.jpmc.balancetracker.receiver.TransactionDTO;

import java.math.BigDecimal;

/**
 * Service to aggregate transactions tracking the overall balance for an account.
 */
public interface BankAccountService {
    /**
     * Process a given transaction - this is to be called by the credit and debit generation threads.
     *
     * @param transactionDTO transaction to process
     */
    void processTransaction(TransactionDTO transactionDTO) throws BalanceTrackerException;

    /**
     * Retrieve the balance in the account
     */
    BigDecimal retrieveBalance(String accountId) throws BalanceTrackerException;

}