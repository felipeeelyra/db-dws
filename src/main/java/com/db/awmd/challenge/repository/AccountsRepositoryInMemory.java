package com.db.awmd.challenge.repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.AccountIdNotFoundException;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;

import org.springframework.stereotype.Repository;

@Repository
public class AccountsRepositoryInMemory implements AccountsRepository {

  private final Map<String, Account> accounts = new ConcurrentHashMap<>();

  @Override
  public void createAccount(Account account) throws DuplicateAccountIdException {
    Account previousAccount = accounts.putIfAbsent(account.getAccountId(), account);
    if (previousAccount != null) {
      throw new DuplicateAccountIdException(
        "Account id " + account.getAccountId() + " already exists!");
    }
  }

  @Override
  public Account getAccount(String accountId) {
    Account account = accounts.get(accountId);
    if (account == null)
      throw new AccountIdNotFoundException( "Account id " + accountId + " does not exist!");
    return account;
  }

  @Override
  public void clearAccounts() {
    accounts.clear();
  }

  public void updateAccount(final Account account){
    accounts.put(account.getAccountId(), account);
  }

}
