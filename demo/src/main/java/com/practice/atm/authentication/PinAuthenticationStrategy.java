package com.practice.atm.authentication;

import com.practice.atm.domain.models.Card;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PinAuthenticationStrategy implements AuthenticationStrategy {
    private final RemoteAuthClient remoteAuthClient;

    @Override
    public boolean authenticate(Card card, AuthenticationRequest request) {
        PinAuthenticationRequest request1 = (PinAuthenticationRequest) request;
        return remoteAuthClient.verifyPin(card, request1.pin());
    }
}
