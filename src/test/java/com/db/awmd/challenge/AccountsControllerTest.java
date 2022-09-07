package com.db.awmd.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.service.AccountsService;
import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
public class AccountsControllerTest {

  private MockMvc mockMvc;

  @Autowired
  private AccountsService accountsService;

  @Autowired
  private WebApplicationContext webApplicationContext;

  @Before
  public void prepareMockMvc() {
    this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

    // Reset the existing accounts before each test.
    accountsService.getAccountsRepository().clearAccounts();
  }

  @Test
  public void createAccount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

    Account account = accountsService.getAccount("Id-123");
    assertThat(account.getAccountId()).isEqualTo("Id-123");
    assertThat(account.getBalance()).isEqualByComparingTo("1000");
  }

  @Test
  public void createDuplicateAccount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountNoAccountId() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountNoBalance() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\"}")).andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountNoBody() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountNegativeBalance() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":-1000}")).andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountEmptyAccountId() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"\",\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  public void getAccount() throws Exception {
    String uniqueAccountId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueAccountId, new BigDecimal("123.45"));
    this.accountsService.createAccount(account);
    this.mockMvc.perform(get("/v1/accounts/" + uniqueAccountId))
      .andExpect(status().isOk())
      .andExpect(
        content().string("{\"accountId\":\"" + uniqueAccountId + "\",\"balance\":123.45}"));
  }

  @Test
  public void balanceTransfer_Pass() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"ACC-TEST1-1\",\"balance\":1000}")).andExpect(status().isCreated());

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"ACC-TEST1-2\",\"balance\":1000}")).andExpect(status().isCreated());

    this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
            .content("{\"fromAccount\":\"ACC-TEST1-1\",\"toAccount\":\"ACC-TEST1-2\",\"transferAmount\":300}")).andExpect(status().isCreated());

    Account fromAccount = accountsService.getAccount("ACC-TEST1-1");
    assertThat(fromAccount.getBalance()).isEqualByComparingTo("700");

    Account toAccount = accountsService.getAccount("ACC-TEST1-2");
    assertThat(toAccount.getBalance()).isEqualByComparingTo("1300");

  }

  @Test
  public void addAccount_failsOnNullFromAccount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"ACC-TEST2-2\",\"balance\":1000}")).andExpect(status().isCreated());

    this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
            .content("{\"fromAccount\":\"ACC-TEST2-1\",\"toAccount\":\"ACC-TEST2-2\",\"transferAmount\":300}")).andExpect(status().isBadRequest());
  }

  @Test
  public void addAccount_failsOnNullToAccount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"ACC-TEST3-1\",\"balance\":1000}")).andExpect(status().isCreated());

    this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
            .content("{\"fromAccount\":\"ACC-TEST3-1\",\"toAccount\":\"ACC-TEST3-2\",\"transferAmount\":300}")).andExpect(status().isBadRequest());
  }

  @Test
  public void addAccount_failsOnNegativeTransferAmount() throws Exception {
   this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
            .content("{\"fromAccount\":\"ACC-TEST3-1\",\"toAccount\":\"ACC-TEST3-2\",\"transferAmount\":-300}")).andExpect(status().isBadRequest());
  }

  @Test
  public void addAccount_failsOnInsufficientFunds() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"ACC-TEST4-1\",\"balance\":100}")).andExpect(status().isCreated());

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"ACC-TEST4-2\",\"balance\":1000}")).andExpect(status().isCreated());

    this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
            .content("{\"fromAccount\":\"ACC-TEST4-1\",\"toAccount\":\"ACC-TEST4-2\",\"transferAmount\":300}")).andExpect(status().isBadRequest());
  }

  @Test
  public void balanceTransferConcurrent_Pass1() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"ACC-TEST5-1\",\"balance\":1000}")).andExpect(status().isCreated());

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"ACC-TEST5-2\",\"balance\":1000}")).andExpect(status().isCreated());

    MvcResult mvcResult1 = (MvcResult) this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
            .content("{\"fromAccount\":\"ACC-TEST5-1\",\"toAccount\":\"ACC-TEST5-2\",\"transferAmount\":100}")).andReturn();

    MvcResult mvcResult2 = (MvcResult) this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
            .content("{\"fromAccount\":\"ACC-TEST5-1\",\"toAccount\":\"ACC-TEST5-2\",\"transferAmount\":100}")).andReturn();

    MvcResult mvcResult3 = (MvcResult) this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
            .content("{\"fromAccount\":\"ACC-TEST5-1\",\"toAccount\":\"ACC-TEST5-2\",\"transferAmount\":100}")).andReturn();

    MvcResult mvcResult4 = (MvcResult) this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
            .content("{\"fromAccount\":\"ACC-TEST5-1\",\"toAccount\":\"ACC-TEST5-2\",\"transferAmount\":100}")).andReturn();

    MvcResult mvcResult5 = (MvcResult) this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
            .content("{\"fromAccount\":\"ACC-TEST5-1\",\"toAccount\":\"ACC-TEST5-2\",\"transferAmount\":100}")).andReturn();

    MvcResult mvcResult6 = (MvcResult) this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
            .content("{\"fromAccount\":\"ACC-TEST5-1\",\"toAccount\":\"ACC-TEST5-2\",\"transferAmount\":100}")).andReturn();

    Thread t1 = new Thread(new RestCallTask(this.mockMvc,mvcResult1));
    Thread t2 = new Thread(new RestCallTask(this.mockMvc,mvcResult2));
    Thread t3 = new Thread(new RestCallTask(this.mockMvc,mvcResult3));
    Thread t4 = new Thread(new RestCallTask(this.mockMvc,mvcResult4));
    Thread t5 = new Thread(new RestCallTask(this.mockMvc,mvcResult5));
    Thread t6 = new Thread(new RestCallTask(this.mockMvc,mvcResult6));

    t1.start();
    t2.start();
    t3.start();
    t4.start();
    t5.start();
    t6.start();

    t1.join();
    t2.join();
    t3.join();
    t4.join();
    t5.join();
    t6.join();

    Account fromAccount = accountsService.getAccount("ACC-TEST5-1");
    assertThat(fromAccount.getBalance()).isEqualByComparingTo("400");

    Account toAccount = accountsService.getAccount("ACC-TEST5-2");
    assertThat(toAccount.getBalance()).isEqualByComparingTo("1600");

  }

}

class RestCallTask implements Runnable {

  private final MockMvc mockMvc;
  private final MvcResult mvcResult;

  public RestCallTask(MockMvc mockMvc, MvcResult mvcResult) {
    this.mockMvc = mockMvc;
    this.mvcResult = mvcResult;
  }


  @Override
  public void run() {
    try {
      mockMvc.perform(asyncDispatch(mvcResult)).andExpect(status().isCreated());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
