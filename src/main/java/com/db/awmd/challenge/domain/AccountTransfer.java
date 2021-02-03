package com.db.awmd.challenge.domain;

import java.math.BigDecimal;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.hibernate.validator.constraints.NotEmpty;

import lombok.Data;

@Data
public class AccountTransfer {

  @NotNull
  @NotEmpty
  private String accountTo;

  @NotNull
  private BigDecimal amount;

  public AccountTransfer() {
  }

  @JsonCreator
  public AccountTransfer(@JsonProperty("accountTo") String accountTo, @JsonProperty("amount") BigDecimal amount) {
    this.accountTo = accountTo;
    this.amount = amount;
  }
}
