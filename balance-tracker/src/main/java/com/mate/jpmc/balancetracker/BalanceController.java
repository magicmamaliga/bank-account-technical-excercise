package com.mate.jpmc.balancetracker;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
public class BalanceController {

    BankAccountService bankAccountService;

    public BalanceController(BankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }

    @GetMapping("/balance")
    public BalanceRecord getBalance() {
        return bankAccountService.getBalance();
    }


}
