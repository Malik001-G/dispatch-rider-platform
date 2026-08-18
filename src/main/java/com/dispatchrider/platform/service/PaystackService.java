package com.dispatchrider.platform.service;

import com.dispatchrider.platform.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * FR-7c: Paystack integration (card + bank transfer, standard for Nigerian small-transaction
 * volume) rather than manual bank transfer confirmation.
 * Amounts are in kobo (Paystack's smallest unit) - naira * 100.
 */
@Service
public class PaystackService {

    private final WebClient webClient;
    private final String secretKey;
    private final String callbackUrl;

    public PaystackService(@Value("${app.paystack.base-url}") String baseUrl,
                            @Value("${app.paystack.secret-key}") String secretKey,
                            @Value("${app.paystack.callback-url}") String callbackUrl) {
        this.secretKey = secretKey;
        this.callbackUrl = callbackUrl;
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + secretKey)
                .build();
    }

    /** Initializes a transaction and returns the Paystack authorization URL + reference. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> initializeTransaction(String email, int amountNaira, String orderReference) {
        if (secretKey == null || secretKey.isBlank()) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Payment gateway not configured (PAYSTACK_SECRET_KEY missing)");
        }

        Map<String, Object> requestBody = Map.of(
                "email", email,
                "amount", amountNaira * 100, // kobo
                "reference", orderReference,
                "callback_url", callbackUrl
        );

        Map<String, Object> response = webClient.post()
                .uri("/transaction/initialize")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        return (Map<String, Object>) response.get("data");
    }

    /** Verifies the X-Paystack-Signature header on inbound webhooks (HMAC-SHA512 of the raw body). */
    public boolean verifyWebhookSignature(String rawBody, String signatureHeader) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] hash = mac.doFinal(rawBody.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString().equals(signatureHeader);
        } catch (Exception e) {
            return false;
        }
    }

    /** FR-7f/FR-7g: refund a specific amount against a transaction reference. */
    public void refund(String transactionReference, Integer amountNairaOrNullForFull) {
        Map<String, Object> body = amountNairaOrNullForFull == null
                ? Map.of("transaction", transactionReference)
                : Map.of("transaction", transactionReference, "amount", amountNairaOrNullForFull * 100);

        webClient.post()
                .uri("/refund")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }
}
