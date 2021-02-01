package com.db.awmd.challenge.web;

import javax.validation.Valid;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.AccountTransfer;
import com.db.awmd.challenge.exception.AccountIdNotFoundException;
import com.db.awmd.challenge.exception.BalanceNotAvailableException;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.TransferAmountShouldNotBeZero;
import com.db.awmd.challenge.service.AccountsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/v1/accounts")
@Slf4j
public class AccountsController {

  private final AccountsService accountsService;

  @Autowired
  public AccountsController(AccountsService accountsService) {
    this.accountsService = accountsService;
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> createAccount(@RequestBody @Valid final Account account) {
    log.info("Creating account {}", account);

    try {
      this.accountsService.createAccount(account);
    } catch (DuplicateAccountIdException daie) {
      return new ResponseEntity<>(daie.getMessage(), HttpStatus.BAD_REQUEST);
    }

    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  @RequestMapping("/{accountFrom}/transfer/")
  public ResponseEntity<Object> transferMoney(@PathVariable final String accountFrom,
      @RequestBody @Valid final AccountTransfer accountTransfer) {
    log.info("Tranfering {} from account {} to {} ", accountTransfer.getAmount(), accountFrom,
        accountTransfer.getAccountTo());

    try {
      this.accountsService.transfer(accountFrom, accountTransfer);
    } catch (TransferAmountShouldNotBeZero tasnbze) {
      return new ResponseEntity<>(tasnbze.getMessage(), HttpStatus.BAD_REQUEST);
    } catch (AccountIdNotFoundException anfe) {
      return new ResponseEntity<>(anfe.getMessage(), HttpStatus.BAD_REQUEST);
    } catch (BalanceNotAvailableException bnae) {
      return new ResponseEntity<>(bnae.getMessage(), HttpStatus.BAD_REQUEST);
    }

    return new ResponseEntity<>(HttpStatus.OK);
  }

  @GetMapping(path = "/{accountId}")
  public Account getAccount(@PathVariable final String accountId) {
    log.info("Retrieving account for id {}", accountId);
    return this.accountsService.getAccount(accountId);
  }

}
