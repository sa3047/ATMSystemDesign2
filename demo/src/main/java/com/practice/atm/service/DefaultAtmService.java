package com.practice.atm.service;

import com.practice.atm.authentication.*;
import com.practice.atm.domain.models.Account;
import com.practice.atm.domain.models.Card;
import com.practice.atm.domain.tx.*;
import com.practice.atm.repositories.AccountRepository;
import com.practice.atm.repositories.CardRepository;
import com.practice.atm.repositories.TransactionRepository;
import com.practice.atm.repositories.infra.InMemorySessionStore;
import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
public class DefaultAtmService implements AtmService {
    private final CardRepository cardRepo;
    private final AccountRepository accountRepo;
    private final TransactionRepository txRepo;
    private final InMemorySessionStore sessionStore;
    private final RemoteAuthClient authClient;
    private final FraudService fraudService;

    @Override
    public AuthSession authenticate(String cardNumber, AuthenticationRequest request) {
        Card card = cardRepo.findByCardNumber(cardNumber)
                .orElseThrow(() -> new IllegalArgumentException("Card not found"));

        if(card.isBlocked()) {
            throw new IllegalStateException("Card is blocked");
        }

//        AuthenticationStrategy strategy = switch (request) {
//            case PinAuthenticationRequest ignored -> new PinAuthenticationStrategy(authClient);
//            //case BiometricAuthenticationRequest ignored -> new Bio(authClient);
//        };
//
        AuthenticationStrategy strategy;

        if(request instanceof PinAuthenticationRequest) {
            strategy = new PinAuthenticationStrategy(authClient);
        } else {
            throw new IllegalArgumentException("Unsupported authentication request type");
        }

        boolean ok = strategy.authenticate(card, request);

        if(!ok) {
            // check failed attempts and block card if necessary
            throw new IllegalArgumentException("Authentication failed");
        }

        return sessionStore.CreateSession(card.getCustomerId().value());
    }

    private AuthSession requireSession(String sessionId) {
        return sessionStore.get(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired session"));
    }

    private String generateTxId() {
        // in real system, call distributed ID generator (Snowflake etc.)
        return UUID.randomUUID().toString();
    }

    @Override
    @Transactional
    public AtmTransaction withdraw(String sessionId, String accountId, Money amount) {
        AuthSession session = requireSession(sessionId);
        fraudService.checkWithdraw(session.customerId(), accountId, amount);

        WithdrawCommand cmd = new WithdrawCommand(
                accountRepo,
                txRepo,
                generateTxId(),
                accountId,
                amount
        );
        return cmd.execute();
    }

    @Override
    @Transactional
    public AtmTransaction deposit(String sessionId, String accountId, Money amount) {
        AuthSession session = requireSession(sessionId);
        fraudService.checkDeposit(session.customerId(), accountId, amount);
        // implement DepositCommand similarly
        AtmTransaction tx = new AtmTransaction(
                generateTxId(),
                TransactionType.DEPOSIT,
                accountId,
                null,
                amount,
                Instant.now(),
                TransactionStatus.PENDING
        );
        txRepo.save(tx);

        Account acc = accountRepo.findByIdForUpdate(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        acc.deposit(amount);
        accountRepo.save(acc);

        tx.markAsSuccess();
        txRepo.save(tx);
        return tx;
    }

    @Override
    @Transactional
    public AtmTransaction transfer(String sessionId, String fromAccountId, String toAccountId, Money amount) {
        AuthSession session = requireSession(sessionId);
        fraudService.checkTransfer(session.customerId(), fromAccountId, toAccountId, amount);

        // naive implementation with 2 accounts locked via DB (or using consistent ordering)
        String txId = generateTxId();
        AtmTransaction tx = new AtmTransaction(
                txId,
                TransactionType.TRANSFER,
                fromAccountId,
                toAccountId,
                amount,
                Instant.now(),
                TransactionStatus.PENDING
        );
        txRepo.save(tx);

        Account from = accountRepo.findByIdForUpdate(fromAccountId)
                .orElseThrow(() -> new IllegalArgumentException("From account not found"));
        Account to = accountRepo.findByIdForUpdate(toAccountId)
                .orElseThrow(() -> new IllegalArgumentException("To account not found"));

        if (from.getBalance().amountInCents() < amount.amountInCents()) {
            tx.markAsFailed();
            txRepo.save(tx);
            throw new IllegalStateException("Insufficient balance");
        }

        from.withdraw(amount);
        to.deposit(amount);

        accountRepo.save(from);
        accountRepo.save(to);

        tx.markAsSuccess();
        txRepo.save(tx);
        return tx;
    }

    @Override
    @Transactional(readOnly = true)
    public Money getBalance(String sessionId, String accountId) {
        AuthSession session = requireSession(sessionId);
        // optional: verify that account belongs to this customer
        Account acc = accountRepo.findByIdForUpdate(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        return acc.getBalance();
    }
}

