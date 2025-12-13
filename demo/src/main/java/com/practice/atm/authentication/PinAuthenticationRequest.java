package com.practice.atm.authentication;

public record PinAuthenticationRequest(String pin) implements AuthenticationRequest {
    
}
