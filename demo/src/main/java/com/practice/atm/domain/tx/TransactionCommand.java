package com.practice.atm.domain.tx;

public interface TransactionCommand {
    AtmTransaction execute();
}

