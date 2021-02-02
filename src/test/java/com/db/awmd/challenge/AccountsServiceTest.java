package com.db.awmd.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.AccountTransfer;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.service.AccountsService;

import org.junit.Before;
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

  @Before
  public void prepareMockMvc() {
    // Reset the existing accounts before each test.
    accountsService.getAccountsRepository().clearAccounts();
  }

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
  public void transfer_WithConcurrency() throws InterruptedException {

    String account1 = "Id-123";
    Account accountA = new Account(account1);
    accountA.setBalance(new BigDecimal(50));
    this.accountsService.createAccount(accountA);

    String account2 = "Id-1234";
    Account accountB = new Account(account2);
    accountB.setBalance(new BigDecimal(50));
    this.accountsService.createAccount(accountB);

    AccountTransfer accTranferAtoB = new AccountTransfer();
    accTranferAtoB.setAccountTo("Id-1234");
    accTranferAtoB.setAmount(new BigDecimal(10));
    AccountTransfer accTranferBtoA = new AccountTransfer();
    accTranferBtoA.setAccountTo("Id-123");
    accTranferBtoA.setAmount(new BigDecimal(5));

    int numberOfThreads = 8;
    ExecutorService service = Executors.newFixedThreadPool(10);
    CountDownLatch latch = new CountDownLatch(numberOfThreads);
    MyCounter counter = new MyCounter();
    for (int i = 0; i < numberOfThreads; i++) {
      service.submit(() -> {
        counter.increment();
        if (counter.getCount() % 2 == 0) {
          this.accountsService.transfer("Id-123", accTranferAtoB);
        } else {
          this.accountsService.transfer("Id-1234", accTranferBtoA);
        }
        latch.countDown();
      });
    }
    latch.await();

    assertEquals(this.accountsService.getAccount("Id-123").getBalance(), new BigDecimal(30));
    assertEquals(this.accountsService.getAccount("Id-1234").getBalance(), new BigDecimal(70));

  }

  public class MyCounter {
    private int count;

    /**
     * @return the count
     */
    public int getCount() {
      return count;
    }

    public void increment() {
      int temp = count;
      count = temp + 1;
    }
  }
}
