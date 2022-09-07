package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.dto.BalanceTransfer;
import com.db.awmd.challenge.exception.BalanceTransferException;
import com.db.awmd.challenge.repository.AccountsRepository;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * PLEASE USE SOLUTION.txt for changes done to solve the challenge.
 */

@Service
@Slf4j
public class AccountsService {

  @Getter
  private final AccountsRepository accountsRepository;

  @Autowired
  public AccountsService(AccountsRepository accountsRepository) {
    this.accountsRepository = accountsRepository;
  }

  @Autowired
  public NotificationService notificationService;

  public void createAccount(Account account) {
    this.accountsRepository.createAccount(account);
  }

  public Account getAccount(String accountId) {
    return this.accountsRepository.getAccount(accountId);
  }

  private Account updateAccount(Account account) {
    return this.accountsRepository.updateAccount(account);
  }

  @Async
  @SneakyThrows
  public CompletableFuture<List<Account>> transferBalance(BalanceTransfer balanceTransfer) {
    List<Account> updatedaccounts = new ArrayList<>();

    if(balanceTransfer.getTransferAmount().compareTo(BigDecimal.ZERO) < 0){
      throw new BalanceTransferException("Exception in balance transfer. Can not transfer negative amount.");
    }

    Account fromAccount = getAccount(balanceTransfer.getFromAccount());
    if(fromAccount == null) {
      throw new BalanceTransferException("Exception in balance transfer. Account ["+balanceTransfer.getFromAccount()+"] does not exist.");
    }

    Account toAccount = getAccount(balanceTransfer.getToAccount());
    if(toAccount == null) {
      throw new BalanceTransferException("Exception in balance transfer. Account ["+balanceTransfer.getToAccount()+"] does not exist.");
    }

    if(fromAccount.getBalance().compareTo(balanceTransfer.getTransferAmount()) < 0)  {
      throw new BalanceTransferException("Exception in balance transfer. Insufficient funds. Account ["+balanceTransfer.getFromAccount()+"] does not have sufficient balance to perform this transfer");
    }

    fromAccount.setBalance(fromAccount.getBalance().subtract(balanceTransfer.getTransferAmount()));
    updatedaccounts.add(updateAccount(fromAccount));

    toAccount.setBalance(toAccount.getBalance().add(balanceTransfer.getTransferAmount()));
    updatedaccounts.add(updateAccount(toAccount));

    notificationService.notifyAboutTransfer(fromAccount, "Amount ["+balanceTransfer.getTransferAmount()+"] debited from Account. Updated balance ["+fromAccount.getBalance()+"]");
    notificationService.notifyAboutTransfer(toAccount, "Amount ["+balanceTransfer.getTransferAmount()+"] credited to Account. Updated balance ["+toAccount.getBalance()+"]");

    log.info("Balance Transfer Success. Server Thread = "+Thread.currentThread().getName());
    return CompletableFuture.completedFuture(updatedaccounts);
  }
}
