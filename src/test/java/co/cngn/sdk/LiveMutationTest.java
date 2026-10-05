package co.cngn.sdk;

import java.math.BigDecimal;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Supplier;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class LiveMutationTest {
    private static final Set<String> CASES = Set.of("createTemporaryVirtualAccount",
            "updateBankAccount", "whitelistAddress", "redeemAsset", "withdraw", "bridge");
    private static CngnClient client;

    @BeforeAll static void connect() throws Exception {
        assumeTrue("1".equals(System.getenv("CNGN_MUTATION_TESTS")),
                "Mutation checks are disabled");
        String selected = System.getenv("CNGN_MUTATION_CASES");
        assumeTrue(selected != null && !selected.isBlank(), "No mutation cases selected");
        for (String name : selected.split(",")) {
            if (!CASES.contains(name.trim())) {
                throw new IllegalStateException("CNGN_MUTATION_CASES contains an unknown case");
            }
        }
        String apiKey = required("CNGN_API_KEY");
        if (!apiKey.startsWith("cngn_test_")) {
            throw new IllegalStateException("Mutation checks require a cngn_test_ API key");
        }
        String configuredPath = System.getenv("CNGN_PRIVATE_KEY_PATH");
        Path keyPath = configuredPath == null || configuredPath.isBlank()
                ? Path.of("../cngn_test_id_ed25519") : Path.of(configuredPath);
        client = CngnClient.builder(apiKey, required("CNGN_ENCRYPTION_KEY"),
                Files.readString(keyPath))
                .environment("test").timeout(Duration.ofSeconds(20)).maxRetries(0).build();
    }

    @AfterAll static void disconnect() {
        if (client != null) client.close();
    }

    private static void selected(String name) {
        String cases = System.getenv("CNGN_MUTATION_CASES");
        assumeTrue(cases != null && Arrays.stream(cases.split(","))
                .map(String::trim).anyMatch(name::equals), "Mutation case is not selected");
    }

    private static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) throw new IllegalStateException(name + " is required");
        return value;
    }

    private static String optional(String name) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? null : value;
    }

    private static BigDecimal decimal(String name) {
        try {
            return new BigDecimal(required(name));
        } catch (NumberFormatException error) {
            throw new IllegalStateException(name + " must be a decimal");
        }
    }

    private static BigDecimal amount(String name, BigDecimal minimum) {
        BigDecimal value = decimal(name);
        BigDecimal maximum = decimal("CNGN_TEST_MAX_AMOUNT");
        if (value.signum() <= 0 || value.compareTo(minimum) < 0
                || maximum.signum() <= 0 || value.compareTo(maximum) > 0) {
            throw new IllegalStateException(name + " is outside the allowed test amount range");
        }
        return value;
    }

    private static String accountNumber(String name) {
        String value = required(name);
        if (!value.matches("[0-9]{10}")) throw new IllegalStateException(name + " must be 10 digits");
        return value;
    }

    private static <T> T check(String name, Supplier<T> operation) {
        try {
            T result = operation.get();
            assertNotNull(result);
            return result;
        } catch (CngnException error) {
            throw new AssertionError(name + " failed: " + error.kind()
                    + " (HTTP " + error.httpStatus() + ")");
        } catch (RuntimeException error) {
            throw new AssertionError(name + " failed: " + error.getClass().getSimpleName());
        }
    }

    @Test void createTemporaryVirtualAccount() {
        selected("createTemporaryVirtualAccount");
        BigDecimal value = amount("CNGN_TEST_TEMPORARY_AMOUNT", new BigDecimal("100"));
        String email = required("CNGN_TEST_CUSTOMER_EMAIL");
        String customerName = required("CNGN_TEST_CUSTOMER_NAME");
        String accountName = required("CNGN_TEST_TEMPORARY_ACCOUNT_NAME");
        String narration = optional("CNGN_TEST_NARRATION");
        if (accountName.length() < 3 || accountName.length() > 50) {
            throw new IllegalStateException("CNGN_TEST_TEMPORARY_ACCOUNT_NAME must be 3 to 50 characters");
        }
        if (narration != null && narration.length() > 100) {
            throw new IllegalStateException("CNGN_TEST_NARRATION must be at most 100 characters");
        }
        String key = required("CNGN_TEST_TEMPORARY_IDEMPOTENCY_KEY");
        var result = check("createTemporaryVirtualAccount", () -> client.createTemporaryVirtualAccount(
                value, email, customerName, accountName, narration, key));
        assertFalse(result.reference().isBlank());
        assertFalse(result.accountNumber().isBlank());
    }

    @Test void updateBankAccount() {
        selected("updateBankAccount");
        String bankName = required("CNGN_TEST_BANK_NAME");
        String accountName = required("CNGN_TEST_BANK_ACCOUNT_NAME");
        String accountNumber = accountNumber("CNGN_TEST_ACCOUNT_NUMBER");
        check("updateBankAccount", () -> client.updateBankAccount(
                bankName, accountName, accountNumber));
    }

    @Test void whitelistAddress() {
        selected("whitelistAddress");
        String networkId = required("CNGN_TEST_WHITELIST_NETWORK_ID");
        String address = required("CNGN_TEST_WHITELIST_ADDRESS");
        var result = check("whitelistAddress", () -> client.whitelistAddress(networkId, address));
        assertTrue(result.isArray());
        boolean found = false;
        for (var entry : result) {
            if (address.equalsIgnoreCase(entry.path("publicKey").asText())) found = true;
        }
        assertTrue(found);
    }

    @Test void redeemAsset() {
        selected("redeemAsset");
        BigDecimal value = amount("CNGN_TEST_REDEEM_AMOUNT", BigDecimal.ONE);
        String bankCode = required("CNGN_TEST_BANK_CODE");
        String accountNumber = accountNumber("CNGN_TEST_ACCOUNT_NUMBER");
        String key = required("CNGN_TEST_REDEEM_IDEMPOTENCY_KEY");
        var result = check("redeemAsset", () -> client.redeemAsset(
                value, bankCode, accountNumber, false, key));
        assertFalse(result.trxRef().isBlank());
        assertFalse(result.address().isBlank());
    }

    @Test void withdraw() {
        selected("withdraw");
        BigDecimal value = amount("CNGN_TEST_WITHDRAW_AMOUNT", BigDecimal.ZERO);
        String address = required("CNGN_TEST_WITHDRAW_ADDRESS");
        String networkId = required("CNGN_TEST_WITHDRAW_NETWORK_ID");
        String key = required("CNGN_TEST_WITHDRAW_IDEMPOTENCY_KEY");
        var result = check("withdraw", () -> client.withdraw(value, address, networkId, false, key));
        assertFalse(result.trxRef().isBlank());
        var verified = check("verifyWithdrawal", () -> client.verifyWithdrawal(result.trxRef()));
        assertEquals(result.trxRef(), verified.trxRef());
    }

    @Test void bridge() {
        selected("bridge");
        String origin = required("CNGN_TEST_ORIGIN_NETWORK_ID");
        String destination = required("CNGN_TEST_DESTINATION_NETWORK_ID");
        String address = required("CNGN_TEST_DESTINATION_ADDRESS");
        String sender = optional("CNGN_TEST_SENDER_ADDRESS");
        String callback = optional("CNGN_TEST_CALLBACK_URL");
        if (callback != null) {
            try {
                URI uri = URI.create(callback);
                if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                        || uri.getUserInfo() != null) {
                    throw new IllegalArgumentException();
                }
            } catch (IllegalArgumentException error) {
                throw new IllegalStateException("CNGN_TEST_CALLBACK_URL must be HTTPS");
            }
        }
        String key = required("CNGN_TEST_BRIDGE_IDEMPOTENCY_KEY");
        var result = check("bridge", () -> client.bridge(origin, destination, address, sender, callback, key));
        assertFalse(result.receivableAddress().isBlank());
        assertFalse(result.reference().isBlank());
    }
}
