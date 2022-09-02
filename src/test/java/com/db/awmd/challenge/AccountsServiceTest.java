package com.db.awmd.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.dto.BalanceTransfer;
import com.db.awmd.challenge.exception.BalanceTransferException;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.service.AccountsService;
import java.math.BigDecimal;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountsServiceTest {

  @Autowired
  private AccountsService accountsService;

  @Test
  public void addAccount() throws Exception {
    Account account = new Account("Id-123");
    account.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account);

    assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
  }

  @Test
  public void addAccount_failsOnDuplicateId() throws Exception {
    String uniqueId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueId);
    this.accountsService.createAccount(account);

    try {
      this.accountsService.createAccount(account);
      fail("Should have failed when adding duplicate account");
    } catch (DuplicateAccountIdException ex) {
      assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
    }

  }

  @Test
  public void balanceTransfer_Pass() throws Exception {
    Account account1 = new Account("ACC-TEST1-1");
    account1.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account1);

    Account account2 = new Account("ACC-TEST1-2");
    account2.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account2);

    BalanceTransfer balanceTransfer = new BalanceTransfer("ACC-TEST1-1", "ACC-TEST1-2", new BigDecimal(300));
    this.accountsService.transferBalance(balanceTransfer);

    assertThat(this.accountsService.getAccount("ACC-TEST1-1").getBalance()).isEqualTo(new BigDecimal(700));
    assertThat(this.accountsService.getAccount("ACC-TEST1-2").getBalance()).isEqualTo(new BigDecimal(1300));
  }

  @Test
  public void addAccount_failsOnNullFromAccount() throws Exception {

    Account account2 = new Account("ACC-TEST2-2");
    account2.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account2);

    BalanceTransfer balanceTransfer = new BalanceTransfer("ACC-TEST2-1", "ACC-TEST2-2", new BigDecimal(300));

    try {
      this.accountsService.transferBalance(balanceTransfer);
      fail("Should have failed because From Account does not exist");
    } catch (BalanceTransferException ex) {
      assertThat(ex.getMessage()).isEqualTo("Exception in balance transfer. Account [ACC-TEST2-1] does not exist.");
    }

  }

  @Test
  public void addAccount_failsOnNullToAccount() throws Exception {

    Account account2 = new Account("ACC-TEST3-2");
    account2.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account2);

    BalanceTransfer balanceTransfer = new BalanceTransfer("ACC-TEST3-2", "ACC-TEST3-1", new BigDecimal(300));

    try {
      this.accountsService.transferBalance(balanceTransfer);
      fail("Should have failed because To Account does not exist");
    } catch (BalanceTransferException ex) {
      assertThat(ex.getMessage()).isEqualTo("Exception in balance transfer. Account [ACC-TEST3-1] does not exist.");
    }

  }

  @Test
  public void addAccount_failsOnNegativeTransferAmount() throws Exception {

    BalanceTransfer balanceTransfer = new BalanceTransfer("ACC-2", "ACC-1", new BigDecimal(-300));

    try {
      this.accountsService.transferBalance(balanceTransfer);
      fail("Should have failed because negative transfer amount");
    } catch (BalanceTransferException ex) {
      assertThat(ex.getMessage()).isEqualTo("Exception in balance transfer. Can not transfer negative amount.");
    }

  }

  @Test
  public void addAccount_failsOnInsufficientFunds() throws Exception {

    Account account1 = new Account("ACC-1");
    account1.setBalance(new BigDecimal(100));
    this.accountsService.createAccount(account1);

    Account account2 = new Account("ACC-2");
    account2.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account2);

    BalanceTransfer balanceTransfer = new BalanceTransfer("ACC-1", "ACC-2", new BigDecimal(300));

    try {
      this.accountsService.transferBalance(balanceTransfer);
      fail("Should have failed because From Account does nto exist");
    } catch (BalanceTransferException ex) {
      assertThat(ex.getMessage()).isEqualTo("Exception in balance transfer. Insufficient funds. Account [ACC-1] does not have sufficient balance to perform this transfer");
    }

  }

}
