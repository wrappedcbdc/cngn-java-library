package co.cngn.sdk;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HexFormat;

/** cNGN webhook verification. Verify the raw body before parsing it. */
public final class Webhooks {
    private static final ObjectMapper JSON = new ObjectMapper();

    private Webhooks() {}

    public static boolean verify(byte[] rawBody, String signatureHeader, String secret) {
        if (rawBody == null || signatureHeader == null || secret == null || secret.isBlank()
                || !signatureHeader.startsWith("sha256=")) return false;
        String hex = signatureHeader.substring(7).trim();
        if (hex.length() != 64) return false;
        try {
            byte[] supplied = HexFormat.of().parseHex(hex);
            byte[] key = secret.getBytes(StandardCharsets.UTF_8);
            try {
                Mac hmac = Mac.getInstance("HmacSHA256");
                hmac.init(new SecretKeySpec(key, "HmacSHA256"));
                return MessageDigest.isEqual(hmac.doFinal(rawBody), supplied);
            } finally {
                Arrays.fill(key, (byte) 0);
            }
        } catch (Exception e) {
            return false;
        }
    }

    public static Event parse(byte[] rawBody) {
        try {
            JsonNode payload = JSON.readTree(rawBody);
            if (payload == null || !payload.isObject() || !payload.path("event").isTextual()
                    || !payload.path("data").isObject()) {
                throw new IllegalArgumentException("Webhook envelope requires event and data");
            }
            return new Event(payload.get("event").textValue(), payload.get("data"),
                    payload.path("timestamp").isTextual() ? payload.get("timestamp").textValue() : null);
        } catch (Exception e) {
            throw new CngnException(CngnException.Kind.VALIDATION, "Invalid cNGN webhook", e);
        }
    }

    public record Event(String event, JsonNode data, String timestamp) {}
}
