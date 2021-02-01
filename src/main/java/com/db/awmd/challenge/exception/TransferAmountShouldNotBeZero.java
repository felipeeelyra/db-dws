package com.db.awmd.challenge.exception;

public class TransferAmountShouldNotBeZero extends RuntimeException {

  public TransferAmountShouldNotBeZero(String message) {
    super(message);
  }
}
