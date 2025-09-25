package com.mate.jpmc.balancetracker.balance;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
public class BalanceController {

    BankAccountServiceImpl bankAccountService;

    public BalanceController(BankAccountServiceImpl bankAccountService) {
        this.bankAccountService = bankAccountService;
    }

    @CrossOrigin(origins = "http://localhost:5173")
    @GetMapping("/balance")
    public BigDecimal getBalance() {
        return bankAccountService.retrieveBalance();
    }


}
