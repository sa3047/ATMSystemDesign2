package com.practice.atm.api;

import com.practice.atm.api.dto.*;
import com.practice.atm.authentication.*;
import com.practice.atm.domain.tx.AtmTransaction;
import com.practice.atm.domain.tx.Money;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/atm")
public class AtmController {

    private final AtmService atmService;

    public AtmController(AtmService atmService) {
        this.atmService = atmService;
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticateResponse> authenticate(@RequestBody AuthenticateRequest request) {
        AuthenticationRequest authReq;

        if("PIN".equalsIgnoreCase(request.method())) {
            authReq = new PinAuthenticationRequest(request.pin());
        } else {
            authReq = new BiometricAuthenticationRequest(request.biometricToken().getBytes());
        }

        AuthSession session = atmService.authenticate(request.cardNumber(), authReq);

        AuthenticateResponse response = new AuthenticateResponse(
                session.sessionId(),
                session.customerId(),
                session.expiresAt().getEpochSecond()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(@RequestBody WithdrawRequest request) {
        Money money = new Money(request.amount().amountInCents(), request.amount().currency());
        AtmTransaction tx = atmService.withdraw(request.sessionId(), request.accountId(), money);
        return ResponseEntity.ok(toResponse(tx));
    }

    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponse> deposit(@RequestBody DepositRequest request) {
        Money money = new Money(request.amount().amountInCents(), request.amount().currency());
        AtmTransaction tx = atmService.deposit(request.sessionId(), request.accountId(), money);
        return ResponseEntity.ok(toResponse(tx));
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(@RequestBody TransferRequest request) {
        Money money = new Money(request.amount().amountInCents(), request.amount().currency());
        AtmTransaction tx = atmService.transfer(
                request.sessionId(),
                request.fromAccountId(),
                request.toAccountId(),
                money
        );
        return ResponseEntity.ok(toResponse(tx));
    }

    @GetMapping("/balance")
    public ResponseEntity<BalanceResponse> balance(
            @RequestParam String sessionId,
            @RequestParam String accountId
    ) {
        Money balance = atmService.getBalance(sessionId, accountId);
        return ResponseEntity.ok(
                new BalanceResponse(new MoneyDto(balance.amountInCents(), balance.currency()))
        );
    }

    private TransactionResponse toResponse(AtmTransaction tx) {
        MoneyDto m = new MoneyDto(tx.getAmount().amountInCents(), tx.getAmount().currency());
        return new TransactionResponse(
                tx.getTransactionId(),
                tx.getType().name(),
                tx.getStatus().name(),
                m
        );
    }
}
