package co.cngn.sdk;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class LiveReadOnlyTest {
    private static CngnClient client;

    @BeforeAll static void connect() throws Exception {
        assumeTrue("1".equals(System.getenv("CNGN_LIVE_TESTS")), "Live checks are disabled");
        String apiKey = System.getenv("CNGN_API_KEY");
        if (apiKey == null || !apiKey.startsWith("cngn_test_")) {
            throw new IllegalStateException("Live checks require a cngn_test_ API key");
        }
        String encryptionKey = System.getenv("CNGN_ENCRYPTION_KEY");
        if (encryptionKey == null || encryptionKey.isBlank()) {
            throw new IllegalStateException("CNGN_ENCRYPTION_KEY is required");
        }
        String configuredPath = System.getenv("CNGN_PRIVATE_KEY_PATH");
        Path keyPath = configuredPath == null || configuredPath.isBlank()
                ? Path.of("../cngn_test_id_ed25519") : Path.of(configuredPath);
        client = CngnClient.builder(apiKey, encryptionKey, Files.readString(keyPath))
                .environment("test").timeout(Duration.ofSeconds(20)).maxRetries(0).build();
    }

    @AfterAll static void disconnect() {
        if (client != null) client.close();
    }

    @Test void balance() { assertNotNull(client.getBalance()); }

    @Test void transactions() { assertNotNull(client.getTransactions(1, 10)); }

    @Test void networks() { assertNotNull(client.getNetworks()); }

    @Test void networksWithBlockchain() { assertNotNull(client.getNetworks(true)); }

    @Test void virtualAccounts() { assertNotNull(client.getVirtualAccounts()); }

    @Test void banks() { assertNotNull(client.getBanks()); }

    @Test void whitelistedAddresses() { assertNotNull(client.getWhitelistedAddresses(false)); }

    @Test void whitelistedAddressesWithNetwork() {
        assertNotNull(client.getWhitelistedAddresses(true));
    }

    @Test void transactionIteration() {
        var transactions = client.iterateTransactions(10).iterator();
        if (transactions.hasNext()) assertNotNull(transactions.next());
    }

    @Test void bankAccountVerification() {
        String bankCode = System.getenv("CNGN_TEST_BANK_CODE");
        String accountNumber = System.getenv("CNGN_TEST_ACCOUNT_NUMBER");
        assumeTrue(bankCode != null && !bankCode.isBlank()
                && accountNumber != null && !accountNumber.isBlank(),
                "No test bank account supplied");
        assertNotNull(client.verifyBankAccount(bankCode, accountNumber));
    }

    @Test void bridgeQuote() {
        String amount = System.getenv("CNGN_TEST_BRIDGE_AMOUNT");
        String origin = System.getenv("CNGN_TEST_ORIGIN_NETWORK_ID");
        String destination = System.getenv("CNGN_TEST_DESTINATION_NETWORK_ID");
        String address = System.getenv("CNGN_TEST_DESTINATION_ADDRESS");
        assumeTrue(amount != null && !amount.isBlank()
                && origin != null && !origin.isBlank()
                && destination != null && !destination.isBlank()
                && address != null && !address.isBlank(),
                "No test bridge quote fixture supplied");
        assertNotNull(client.getBridgeQuote(new BigDecimal(amount), origin, destination, address));
    }

    @Test void withdrawalVerification() {
        String reference = System.getenv("CNGN_TEST_WITHDRAWAL_REFERENCE");
        assumeTrue(reference != null && !reference.isBlank(), "No test withdrawal reference supplied");
        assertNotNull(client.verifyWithdrawal(reference));
    }

    @Test void webhookSignature() throws Exception {
        String signingSecret = System.getenv("CNGN_SIGNING_SECRET");
        assumeTrue(signingSecret != null && !signingSecret.isBlank(),
                "No webhook signing secret supplied");
        byte[] body = "{\"event\":\"sdk.test\",\"data\":{\"id\":\"fixture\"}}"
                .getBytes(StandardCharsets.UTF_8);
        byte[] key = signingSecret.getBytes(StandardCharsets.UTF_8);
        String signature;
        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            hmac.init(new SecretKeySpec(key, "HmacSHA256"));
            signature = "sha256=" + HexFormat.of().formatHex(hmac.doFinal(body));
        } finally {
            Arrays.fill(key, (byte) 0);
        }
        assertTrue(Webhooks.verify(body, signature, signingSecret));
        byte[] tampered = body.clone();
        tampered[tampered.length - 2] ^= 1;
        assertFalse(Webhooks.verify(tampered, signature, signingSecret));
    }
}
