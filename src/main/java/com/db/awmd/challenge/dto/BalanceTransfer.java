package com.db.awmd.challenge.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
public class BalanceTransfer {
    private final String fromAccount;
    private final String toAccount;
    private final BigDecimal transferAmount;

    public BalanceTransfer(String fromAccount, String toAccount, BigDecimal transferAmount) {
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.transferAmount = transferAmount;
    }
}
