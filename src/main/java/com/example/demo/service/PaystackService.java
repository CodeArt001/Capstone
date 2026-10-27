package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import tools.jackson.databind.JsonNode;

@Service
public class PaystackService {

    private final RestClient restClient;
    private final String secretKey;

    public PaystackService(
            RestClient.Builder restClientBuilder,
            @Value("${paystack.secret-key:}") String secretKey) {
        this.restClient = restClientBuilder.baseUrl("https://api.paystack.co").build();
        this.secretKey = secretKey;
    }

    public PaystackTransaction verifyTransaction(String reference) {
        if (secretKey.isBlank()) {
            throw new IllegalStateException("PAYSTACK_SECRET_KEY is not configured");
        }

        JsonNode response = restClient.get()
                .uri("/transaction/verify/{reference}", reference)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + secretKey)
                .retrieve()
                .body(JsonNode.class);

        JsonNode data = response == null ? null : response.get("data");
        if (response == null
                || !response.path("status").asBoolean(false)
                || data == null
                || !data.path("status").asText().equalsIgnoreCase("success")) {
            throw new IllegalArgumentException("Paystack did not confirm a successful payment");
        }

        String verifiedReference = data.path("reference").asText();
        JsonNode amountNode = data.get("amount");
        String currency = data.path("currency").asText();
        if (verifiedReference.isBlank()
                || !reference.equals(verifiedReference)
                || amountNode == null
                || !amountNode.canConvertToLong()
                || amountNode.longValue() < 0
                || currency.isBlank()) {
            throw new IllegalArgumentException("Paystack returned incomplete transaction details");
        }

        return new PaystackTransaction(
                verifiedReference,
                data.path("status").asText(),
                amountNode.longValue(),
                currency);
    }
}
