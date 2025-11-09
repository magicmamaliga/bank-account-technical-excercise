package com.mate.jpmc.balancetracker.balance;

import com.mate.jpmc.balancetracker.BalanceTrackerException;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
public class BalanceController {

    @Resource
    BankAccountServiceImpl bankAccountService;


    @CrossOrigin(origins = "http://localhost:5173")
    @GetMapping("/balance")
    public BigDecimal getBalance(@RequestParam String accountId) throws BalanceTrackerException {
        return bankAccountService.retrieveBalance(accountId);
    }


}
