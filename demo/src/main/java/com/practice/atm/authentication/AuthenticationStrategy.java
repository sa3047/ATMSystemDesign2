package com.practice.atm.authentication;

import com.practice.atm.domain.models.Card;

public interface AuthenticationStrategy {
    boolean authenticate(Card card, AuthenticationRequest request);
}

