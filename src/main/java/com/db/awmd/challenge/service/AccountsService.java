package com.db.awmd.challenge.service;

import java.math.BigDecimal;
import java.util.Random;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.AccountTransfer;
import com.db.awmd.challenge.exception.BalanceNotAvailableException;
import com.db.awmd.challenge.exception.TransferAmountShouldNotBeZero;
import com.db.awmd.challenge.repository.AccountsRepository;
import com.db.awmd.challenge.util.TransactionType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AccountsService {

  @Getter
  private final AccountsRepository accountsRepository;

  @Getter
  private final NotificationService notificationService;

  @Autowired
  public AccountsService(final AccountsRepository accountsRepository, final NotificationService notificationService) {
    this.accountsRepository = accountsRepository;
    this.notificationService = notificationService;
  }

  public void createAccount(final Account account) {
    this.accountsRepository.createAccount(account);
  }

  public Account getAccount(final String accountId) {
    return this.accountsRepository.getAccount(accountId);
  }

  private boolean hasBalanceAvailable(final Account accountFrom, final BigDecimal amount) {
    log.info("Verifying whether Account {} has balance for transfer ", accountFrom.getAccountId());
    return accountFrom.getBalance().compareTo(amount) >= 0;
  }

  public void transfer(final String accountIdFrom, final AccountTransfer accountTransfer) {

    if (accountTransfer.getAmount().compareTo(BigDecimal.ZERO) < 1) {
      throw new TransferAmountShouldNotBeZero("Amount to be transfered must be greater than ZERO");
    }

    final Account accountFrom = this.getAccount(accountIdFrom);
    final Account accountTo = this.getAccount(accountTransfer.getAccountTo());

    if (!hasBalanceAvailable(accountFrom, accountTransfer.getAmount())) {
      throw new BalanceNotAvailableException("There is no available balance on account: " + accountFrom.getAccountId());
    }

    Random number = new Random();
    while (true) {

      if (accountFrom.getAccountId().hashCode() < accountTo.getAccountId().hashCode()) {

        if (accountFrom.getLock().tryLock()) {
          try {
            if (accountTo.getLock().tryLock()) {
              try {
                this.transferAmount(accountIdFrom, accountFrom, accountTo, accountTransfer);
                break;
              } finally {
                accountTo.getLock().unlock();
              }
            }
          } finally {
            accountFrom.getLock().unlock();
          }

          this.sleep(number);
        }
      } else {
        if (accountTo.getLock().tryLock()) {
          try {
            if (accountFrom.getLock().tryLock()) {
              try {
                this.transferAmount(accountIdFrom, accountFrom, accountTo, accountTransfer);
                break;
              } finally {
                accountFrom.getLock().unlock();
              }
            }
          } finally {
            accountTo.getLock().unlock();
          }

          this.sleep(number);
        }
      }

    }

  }

  private void sleep(final Random number) {
    int n = number.nextInt(1000);
    int TIME = 1000 + n; // 1 second + random delay to prevent livelock
    try {
      Thread.sleep(TIME);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private void transferAmount(final String accountIdFrom, final Account accountFrom, final Account accountTo,
      final AccountTransfer accountTransfer) {
    this.updateBalance(accountFrom, accountTransfer.getAmount(), TransactionType.SUBTRACT_MONEY);
    this.updateBalance(accountTo, accountTransfer.getAmount(), TransactionType.ADD_MONEY);

    this.notificationService.notifyAboutTransfer(accountFrom,
        "A Transaction has been made from your Account " + accountIdFrom + " to " + accountTransfer.getAccountTo()
            + " with the amount of " + accountTransfer.getAmount());
    this.notificationService.notifyAboutTransfer(accountTo, "A Transaction has been made to your Account from "
        + accountIdFrom + " with the amount of " + accountTransfer.getAmount());
  }

  private void updateBalance(final Account account, final BigDecimal amount, final TransactionType transactionType) {
    switch (transactionType) {
      case ADD_MONEY:
        account.setBalance(account.getBalance().add(amount));
        break;
      case SUBTRACT_MONEY:
        account.setBalance(account.getBalance().subtract(amount));
        break;
    }

    this.accountsRepository.updateAccount(account);
  }
}
