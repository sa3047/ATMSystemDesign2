package com.practice.atm.repositories.infra;

import com.practice.atm.domain.tx.AtmTransaction;
import com.practice.atm.repositories.TransactionRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryTransactionRepository implements TransactionRepository {

    private final Map<String, AtmTransaction> txs = new ConcurrentHashMap<>();

    @Override
    public Optional<AtmTransaction> findById(String id) {
        return Optional.ofNullable(txs.get(id));
    }

    @Override
    public void save(AtmTransaction transaction) {
        txs.put(transaction.getTransactionId(), transaction);
    }
}
