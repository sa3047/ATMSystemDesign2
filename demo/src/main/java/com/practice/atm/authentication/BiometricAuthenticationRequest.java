package com.practice.atm.authentication;

public record BiometricAuthenticationRequest(byte[] biometricData) implements AuthenticationRequest {
    
}
