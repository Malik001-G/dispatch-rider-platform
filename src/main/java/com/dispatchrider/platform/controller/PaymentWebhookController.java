package com.dispatchrider.platform.controller;

import com.dispatchrider.platform.service.OrderService;
import com.dispatchrider.platform.service.PaystackService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** FR-7d: order only becomes eligible for assignment once Paystack confirms payment via webhook. */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentWebhookController {

    private final OrderService orderService;
    private final PaystackService paystackService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestHeader("X-Paystack-Signature") String signature,
                                                 @RequestBody String rawBody) throws Exception {
        if (!paystackService.verifyWebhookSignature(rawBody, signature)) {
            return ResponseEntity.status(401).body("Invalid signature");
        }

        JsonNode root = objectMapper.readTree(rawBody);
        String event = root.path("event").asText("");

        // Only charge.success matters for FR-7d; other Paystack event types are ignored.
        if ("charge.success".equals(event)) {
            String reference = root.path("data").path("reference").asText(null);
            if (reference != null) {
                orderService.confirmPayment(reference);
            }
        }

        return ResponseEntity.ok("ok");
    }
}
