package com.aaspaas.aaspaas_backend.wallet.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * RazorpayX bank payout (contact -> fund account -> payout).
 * Active only when payout.provider=razorpayx.
 *
 * NOTE: written against RazorpayX public docs but NOT executed against a live
 * account. Test with RazorpayX test mode before going live.
 */
@Component
@ConditionalOnProperty(name = "payout.provider", havingValue = "razorpayx")
public class RazorpayXPayoutProvider implements PayoutProvider {

    private final ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Value("${payout.razorpayx.key-id}")
    private String keyId;

    @Value("${payout.razorpayx.key-secret}")
    private String keySecret;

    @Value("${payout.razorpayx.account-number}")
    private String accountNumber;

    @Value("${payout.razorpayx.webhook-secret:}")
    private String webhookSecret;

    @Value("${payout.razorpayx.base-url:https://api.razorpay.com/v1}")
    private String baseUrl;

    @Value("${payout.razorpayx.mode:IMPS}")
    private String mode;

    public RazorpayXPayoutProvider(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    void validate() {
        if (isBlank(keyId) || isBlank(keySecret) || isBlank(accountNumber)) {
            throw new IllegalStateException(
                    "payout.razorpayx.key-id, key-secret and account-number are required");
        }
    }

    @Override
    public PayoutResult transfer(PayoutInstruction in) {
        try {
            // 1. contact
            Map<String, Object> contact = new LinkedHashMap<>();
            contact.put("name", in.accountHolderName());
            contact.put("type", "vendor");
            contact.put("reference_id", in.reference());
            String contactId = post("/contacts", contact, null).path("id").asText(null);
            require(contactId, "contact id");

            // 2. fund account
            Map<String, Object> bank = new LinkedHashMap<>();
            bank.put("name", in.accountHolderName());
            bank.put("ifsc", in.ifscCode());
            bank.put("account_number", in.accountNumber());
            Map<String, Object> fund = new LinkedHashMap<>();
            fund.put("contact_id", contactId);
            fund.put("account_type", "bank_account");
            fund.put("bank_account", bank);
            String fundAccountId = post("/fund_accounts", fund, null).path("id").asText(null);
            require(fundAccountId, "fund account id");

            // 3. payout (idempotent through the header)
            Map<String, Object> payout = new LinkedHashMap<>();
            payout.put("account_number", accountNumber);
            payout.put("fund_account_id", fundAccountId);
            payout.put("amount", toPaise(in.amount()));
            payout.put("currency", "INR");
            payout.put("mode", mode);
            payout.put("purpose", "payout");
            payout.put("queue_if_low_balance", false);
            payout.put("reference_id", in.reference());
            payout.put("narration", in.narration());
            JsonNode res = post("/payouts", payout, in.reference());

            String payoutId = res.path("id").asText(null);
            require(payoutId, "payout id");
            return new PayoutResult(payoutId, map(res.path("status").asText("")),
                    res.path("failure_reason").asText(null));

        } catch (PayoutProviderException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new PayoutProviderException("RazorpayX transfer failed", ex);
        }
    }

    @Override
    public boolean verifyWebhookSignature(String rawBody, String signature) {
        if (isBlank(rawBody) || isBlank(signature) || isBlank(webhookSecret)) {
            return false;
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                    webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(rawBody.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return MessageDigest.isEqual(
                    sb.toString().getBytes(StandardCharsets.UTF_8),
                    signature.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public String name() {
        return "RAZORPAYX";
    }

    /** Maps a RazorpayX payout status to our outcome. */
    public static Outcome map(String status) {
        return switch (status == null ? "" : status.toLowerCase()) {
            case "processed" -> Outcome.PAID;
            case "reversed", "cancelled", "rejected", "failed" -> Outcome.FAILED;
            default -> Outcome.PROCESSING; // queued, pending, processing, scheduled
        };
    }

    private JsonNode post(String path, Object body, String idempotencyKey) throws Exception {
        String auth = "Basic " + Base64.getEncoder().encodeToString(
                (keyId + ":" + keySecret).getBytes(StandardCharsets.UTF_8));
        HttpRequest.Builder rb = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Authorization", auth)
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(25))
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)));
        if (idempotencyKey != null) {
            rb.header("X-Payout-Idempotency", idempotencyKey);
        }
        HttpResponse<String> resp = httpClient.send(rb.build(), HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
            // Do not echo the body: it may contain bank details.
            throw new PayoutProviderException(
                    "RazorpayX " + path + " failed with HTTP " + resp.statusCode());
        }
        return objectMapper.readTree(resp.body());
    }

    private long toPaise(BigDecimal amount) {
        try {
            return amount.movePointRight(2).longValueExact();
        } catch (ArithmeticException ex) {
            throw new PayoutProviderException("Amount must have at most 2 decimal places", ex);
        }
    }

    private void require(String value, String what) {
        if (isBlank(value)) {
            throw new PayoutProviderException("RazorpayX response missing " + what);
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
