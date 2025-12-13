package com.practice.atm.domain.tx;

import com.practice.atm.domain.models.Account;
import com.practice.atm.repositories.AccountRepository;
import com.practice.atm.repositories.TransactionRepository;
import lombok.AllArgsConstructor;

import java.time.Instant;

// Similar classes: DepositCommand, TransferCommand, BalanceInquiryCommand (read-only, but still can log).

@AllArgsConstructor
public class WithdrawCommand implements TransactionCommand {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final String txId;
    private final String fromAccountId;
    private final Money amount;

    @Override
    public AtmTransaction execute() {
        // idempotency check here: if already processed, return existing transaction
        var existing = transactionRepository.findById(txId);

        if(existing.isPresent()) {
            return existing.get();
        }

        AtmTransaction tx = new AtmTransaction(txId, TransactionType.WITHDRAWAL,
                fromAccountId, null,
                amount, Instant.now(),
                TransactionStatus.PENDING);

        // orElseThrow(Supplier<? extends X>)
        // what is the meaning <? extends X> why not only write X instead of extends in java ?

        Account account = accountRepository.findByIdForUpdate(fromAccountId)
                        .orElseThrow(() -> new IllegalArgumentException("Account not found: " + fromAccountId));

        transactionRepository.save(tx);

        // simple check; in real life consider min balance / overdraft
        if (account.getBalance().amountInCents() < amount.amountInCents()) {
            tx.markAsFailed();
            transactionRepository.save(tx);
            throw new IllegalStateException("Insufficient balance");
        }

        account.withdraw(amount);
        accountRepository.save(account);

        tx.markAsSuccess();
        transactionRepository.save(tx);

        return tx;
    }
}
