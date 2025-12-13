package com.practice.atm.repositories;

import com.practice.atm.domain.tx.AtmTransaction;

import java.util.Optional;

/*
*
* */
public interface TransactionRepository {
    Optional<AtmTransaction> findById(String transactionId);
    void save(AtmTransaction transaction);
}
