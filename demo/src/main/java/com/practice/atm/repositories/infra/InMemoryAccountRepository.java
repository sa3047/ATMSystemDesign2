package com.practice.atm.repositories.infra;

import com.practice.atm.domain.models.Account;
import com.practice.atm.domain.models.Card;
import com.practice.atm.domain.tx.AtmTransaction;
import com.practice.atm.repositories.AccountRepository;
import com.practice.atm.repositories.CardRepository;
import com.practice.atm.repositories.TransactionRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryAccountRepository implements AccountRepository {

    private final Map<String, Account> accounts = new ConcurrentHashMap<>();

    /* 🏆 Intuition
        Think of Optional.ofNullable(x) as saying:
        “Wrap this value in a box. If it’s there, great — if not, give me an empty box instead of null.”
        It’s a way to make null‑handling explicit and safe.
     */
    @Override
    public Optional<Account> findByIdForUpdate(String accountId) {
       return Optional.ofNullable(accounts.get(accountId));
    }

    @Override
    public void save(Account account) {
        accounts.put(account.getId().toString(), account);
    }

    public void put(Account account) {
        save(account);
    }
}

