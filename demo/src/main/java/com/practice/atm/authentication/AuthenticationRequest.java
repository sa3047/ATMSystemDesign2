package com.practice.atm.authentication;

public sealed interface AuthenticationRequest permits PinAuthenticationRequest, BiometricAuthenticationRequest {}
