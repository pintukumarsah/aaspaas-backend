package com.aaspaas.aaspaas_backend.payment.gateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Component
@RequiredArgsConstructor
public class RazorpayGatewayClient
        implements PaymentGatewayClient {

    private final ObjectMapper objectMapper;

    private final HttpClient httpClient =
            HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_2)
                    .build();

    @Value("${payment.razorpay.key-id}")
    private String keyId;

    @Value("${payment.razorpay.key-secret}")
    private String keySecret;

    @Value("${payment.razorpay.webhook-secret}")
    private String webhookSecret;

    @Value("${payment.razorpay.base-url:https://api.razorpay.com/v1}")
    private String baseUrl;

    @Override
    public GatewayOrder createOrder(
            BigDecimal amount,
            String receipt
    ) {

        long amountInPaise =
                toPaise(amount);

        String requestBody =
                """
                {
                  "amount": %d,
                  "currency": "INR",
                  "receipt": "%s"
                }
                """.formatted(
                        amountInPaise,
                        escapeJson(receipt)
                );

        HttpResponse<String> response =
                executePost(
                        "/orders",
                        requestBody
                );

        try {

            JsonNode json =
                    objectMapper.readTree(
                            response.body()
                    );

            String gatewayOrderId =
                    json.path("id")
                            .asText(null);

            if (gatewayOrderId == null) {
                throw new GatewayException(
                        "Razorpay order ID missing"
                );
            }

            return new GatewayOrder(
                    gatewayOrderId,
                    json.path("amount")
                            .asLong(),
                    json.path("currency")
                            .asText("INR"),
                    json.path("receipt")
                            .asText(receipt)
            );

        } catch (GatewayException ex) {

            throw ex;

        } catch (Exception ex) {

            throw new GatewayException(
                    "Unable to parse Razorpay order response",
                    ex
            );
        }
    }

    @Override
    public GatewayRefund createRefund(
            String gatewayPaymentId,
            BigDecimal amount,
            String receipt
    ) {

        if (gatewayPaymentId == null
                || gatewayPaymentId.isBlank()) {

            throw new GatewayException(
                    "Gateway payment ID is required for refund"
            );
        }

        long amountInPaise =
                toPaise(amount);

        String requestBody =
                """
                {
                  "amount": %d,
                  "notes": {
                    "receipt": "%s"
                  }
                }
                """.formatted(
                        amountInPaise,
                        escapeJson(receipt)
                );

        HttpResponse<String> response =
                executePost(
                        "/payments/"
                                + gatewayPaymentId
                                + "/refund",
                        requestBody
                );

        try {

            JsonNode json =
                    objectMapper.readTree(
                            response.body()
                    );

            String refundId =
                    json.path("id")
                            .asText(null);

            if (refundId == null) {

                throw new GatewayException(
                        "Razorpay refund ID missing"
                );
            }

            return new GatewayRefund(
                    refundId,
                    json.path("payment_id")
                            .asText(gatewayPaymentId),
                    json.path("amount")
                            .asLong(amountInPaise)
            );

        } catch (GatewayException ex) {

            throw ex;

        } catch (Exception ex) {

            throw new GatewayException(
                    "Unable to parse Razorpay refund response",
                    ex
            );
        }
    }

    @Override
    public boolean verifyPaymentSignature(
            String gatewayOrderId,
            String gatewayPaymentId,
            String signature
    ) {

        if (isBlank(gatewayOrderId)
                || isBlank(gatewayPaymentId)
                || isBlank(signature)) {

            return false;
        }

        String payload =
                gatewayOrderId
                        + "|"
                        + gatewayPaymentId;

        String generated =
                hmacSha256(
                        payload,
                        keySecret
                );

        return constantTimeEquals(
                generated,
                signature
        );
    }

    @Override
    public boolean verifyWebhookSignature(
            String rawBody,
            String signature
    ) {

        if (isBlank(rawBody)
                || isBlank(signature)
                || isBlank(webhookSecret)) {

            return false;
        }

        String generated =
                hmacSha256(
                        rawBody,
                        webhookSecret
                );

        return constantTimeEquals(
                generated,
                signature
        );
    }

    private HttpResponse<String> executePost(
            String path,
            String body
    ) {

        try {

            String credentials =
                    keyId
                            + ":"
                            + keySecret;

            String authorization =
                    "Basic "
                            + Base64
                            .getEncoder()
                            .encodeToString(
                                    credentials.getBytes(
                                            StandardCharsets.UTF_8
                                    )
                            );

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(
                                    URI.create(
                                            baseUrl + path
                                    )
                            )
                            .header(
                                    "Authorization",
                                    authorization
                            )
                            .header(
                                    "Content-Type",
                                    "application/json"
                            )
                            .timeout(
                                    java.time.Duration
                                            .ofSeconds(20)
                            )
                            .POST(
                                    HttpRequest.BodyPublishers
                                            .ofString(body)
                            )
                            .build();

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers
                                    .ofString()
                    );

            if (response.statusCode() < 200
                    || response.statusCode() >= 300) {

                throw new GatewayException(
                        "Razorpay API failed. HTTP status: "
                                + response.statusCode()
                                + ", response: "
                                + response.body()
                );
            }

            return response;

        } catch (GatewayException ex) {

            throw ex;

        } catch (Exception ex) {

            throw new GatewayException(
                    "Razorpay API communication failed",
                    ex
            );
        }
    }

    private long toPaise(
            BigDecimal amount
    ) {

        if (amount == null
                || amount.signum() <= 0) {

            throw new GatewayException(
                    "Payment amount must be greater than zero"
            );
        }

        try {

            return amount
                    .movePointRight(2)
                    .longValueExact();

        } catch (ArithmeticException ex) {

            throw new GatewayException(
                    "Amount must have maximum 2 decimal places",
                    ex
            );
        }
    }

    private String hmacSha256(
            String payload,
            String secret
    ) {

        try {

            Mac mac =
                    Mac.getInstance("HmacSHA256");

            SecretKeySpec key =
                    new SecretKeySpec(
                            secret.getBytes(
                                    StandardCharsets.UTF_8
                            ),
                            "HmacSHA256"
                    );

            mac.init(key);

            byte[] digest =
                    mac.doFinal(
                            payload.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            StringBuilder result =
                    new StringBuilder();

            for (byte b : digest) {

                result.append(
                        String.format(
                                "%02x",
                                b
                        )
                );
            }

            return result.toString();

        } catch (Exception ex) {

            throw new GatewayException(
                    "Unable to generate HMAC signature",
                    ex
            );
        }
    }

    private boolean constantTimeEquals(
            String expected,
            String actual
    ) {

        return MessageDigest.isEqual(
                expected.getBytes(
                        StandardCharsets.UTF_8
                ),
                actual.getBytes(
                        StandardCharsets.UTF_8
                )
        );
    }

    private boolean isBlank(
            String value
    ) {

        return value == null
                || value.isBlank();
    }

    private String escapeJson(
            String value
    ) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}