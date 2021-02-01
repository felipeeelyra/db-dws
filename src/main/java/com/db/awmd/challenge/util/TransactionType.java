package com.db.awmd.challenge.util;

public enum TransactionType {

    ADD_MONEY("ADD"), SUBTRACT_MONEY("SUBTRACT");

    private final String type;

    private TransactionType(final String type) {
        this.type = type;
    }

}
