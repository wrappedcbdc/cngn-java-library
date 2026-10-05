package co.cngn.sdk;

import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

class WebhooksTest {
    @Test void verifiesRawBodyAndParsesEvent() {
        byte[] body = "{\"event\":\"deposit.completed\",\"data\":{\"transactionId\":\"tx1\"}}"
                .getBytes(StandardCharsets.UTF_8);
        String signature = "sha256=ca1bca41ca7d9a48a3599b387eb46ce4f2cabbe158fe1f311bb6dbd744e6ca4d";
        // Known HMAC-SHA256 digest for this fixture.
        assertTrue(Webhooks.verify(body, signature, "test-secret"));
        assertFalse(Webhooks.verify("{}".getBytes(StandardCharsets.UTF_8), signature, "test-secret"));
        assertFalse(Webhooks.verify(body, "sha256=xx", "test-secret"));
        assertFalse(Webhooks.verify(body, signature, ""));
        assertEquals("tx1", Webhooks.parse(body).data().get("transactionId").textValue());
        assertThrows(CngnException.class, () -> Webhooks.parse("[]".getBytes(StandardCharsets.UTF_8)));
    }
}
