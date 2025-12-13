package com.practice.atm.authentication;

import com.practice.atm.domain.models.Card;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
public class RemoteAuthClient {
    private final WebClient webClient;

    public RemoteAuthClient(@Value("${auth.service.url:http://localhost:8081}") String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }


    public boolean verifyPin(Card card, String ping) {
        Map<String, Object> body = Map.of(
                "cardNumber", card.getNumber().value(),
                "pin", ping
        );

        return this.webClient.post()
                .uri("/verify-pin")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Boolean.class)
                .blockOptional()
                .orElse(false);
    }

}
