package co.cngn.sdk;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

class CngnClientTest {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final String API_KEY = "cngn_test_fixture";
    private static final String ENCRYPTION_KEY = "test-encryption-key";
    private HttpServer server;
    private CngnClient client;
    private final List<Captured> calls = new ArrayList<>();
    private final AtomicInteger status = new AtomicInteger(200);
    private final AtomicInteger transientFailures = new AtomicInteger();
    private final AtomicReference<byte[]> responseOverride = new AtomicReference<>();

    record Captured(String method, String path, String query, String authorization,
                    String idempotencyKey, byte[] body) {}

    @BeforeEach void start() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", this::handle);
        server.start();
        byte[] seed = new byte[32]; seed[0] = 1;
        client = CngnClient.builder(API_KEY, ENCRYPTION_KEY, CngnCryptoTest.pem(seed))
                .baseUrl("http://127.0.0.1:" + server.getAddress().getPort() + "/v1/api")
                .maxRetries(0).build();
    }

    @AfterEach void stop() {
        if (client != null) client.close();
        if (server != null) server.stop(0);
    }

    private void handle(HttpExchange exchange) {
        try {
            byte[] body = exchange.getRequestBody().readAllBytes();
            synchronized (calls) {
                calls.add(new Captured(exchange.getRequestMethod(), exchange.getRequestURI().getRawPath(),
                        exchange.getRequestURI().getRawQuery(), exchange.getRequestHeaders().getFirst("Authorization"),
                        exchange.getRequestHeaders().getFirst("Idempotency-Key"), body));
            }
            int responseStatus = transientFailures.getAndUpdate(n -> Math.max(0, n - 1)) > 0
                    ? 503 : status.get();
            String responseData = exchange.getRequestURI().getPath().endsWith("/transactions")
                    ? "{\"data\":[],\"pagination\":{\"count\":0,\"pages\":0,\"isLastPage\":true}}"
                    : exchange.getRequestURI().getPath().contains("/withdraw/verify/") ? "{}"
                    : exchange.getRequestMethod().equals("GET") ? "[]" : "{}";
            String response = responseStatus == 200 ? "{\"status\":200,\"data\":" + responseData + "}"
                    : "{\"status\":" + responseStatus + ",\"message\":\"Service is currently unavailable. Please try again later.\"}";
            byte[] bytes = responseOverride.get() == null
                    ? response.getBytes(StandardCharsets.UTF_8) : responseOverride.get();
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(responseStatus, bytes.length);
            exchange.getResponseBody().write(bytes);
        } catch (Exception e) {
            throw new AssertionError(e);
        } finally {
            exchange.close();
        }
    }

    private Captured last() { synchronized (calls) { return calls.getLast(); } }

    private JsonNode plaintext(Captured captured) throws Exception {
        Map<String, String> envelope = JSON.readValue(captured.body(),
                new com.fasterxml.jackson.core.type.TypeReference<>() {});
        return JSON.readTree(CngnCrypto.decryptAes(envelope, CngnCrypto.aesKey(ENCRYPTION_KEY)));
    }

    @Test void routesCurrentEndpointsAndEncryptsExactPayloads() throws Exception {
        client.getBalance();
        assertEquals("GET", last().method());
        assertEquals("/v1/api/balance", last().path());
        assertEquals(0, last().body().length);
        assertEquals("Bearer " + API_KEY, last().authorization());

        client.getTransactions(2, 20);
        assertTrue(last().query().contains("page=2"));
        assertTrue(last().query().contains("limit=20"));
        client.getNetworks(true);
        assertEquals("includeBlockchain=true", last().query());
        client.getVirtualAccounts();
        assertEquals("/v1/api/virtual-account", last().path());

        client.createTemporaryVirtualAccount(new BigDecimal("5000.25"), "a@example.com",
                "Ada", "Merchant", null, "order-1");
        assertEquals("/v1/api/virtual-account/temporary", last().path());
        assertEquals("order-1", last().idempotencyKey());
        JsonNode account = plaintext(last());
        assertEquals(new BigDecimal("5000.25"), account.path("amount").decimalValue());
        assertEquals("a@example.com", account.path("customer").path("email").textValue());
        assertFalse(account.has("narration"));

        client.redeemAsset(new BigDecimal("100.50"), "058", "0123456789", true, "redeem-1");
        assertEquals("/v1/api/redeemAsset", last().path());
        assertEquals("058", plaintext(last()).path("bankCode").textValue());
        assertTrue(plaintext(last()).path("saveDetails").booleanValue());

        client.verifyBankAccount("058", "0123456789");
        assertEquals("/v1/api/account/verify", last().path());
        client.getBanks();
        assertEquals("/v1/api/banks", last().path());
        client.updateBankAccount("Bank", "Ada", "0123456789");
        assertEquals("PUT", last().method());
        assertEquals("/v1/api/bank-account", last().path());
        assertEquals("Ada", plaintext(last()).path("bankAccountName").textValue());

        client.withdraw(new BigDecimal("20"), "0xabc", "network-id", false, "withdraw-1");
        assertEquals("network-id", plaintext(last()).path("networkId").textValue());
        client.verifyWithdrawal("ref/with space");
        assertEquals("/v1/api/withdraw/verify/ref%2Fwith%20space", last().path());
        client.getBridgeQuote(BigDecimal.ONE, "origin", "destination", "0xabc");
        assertEquals("/v1/api/bridge-quote", last().path());
        assertEquals("origin", plaintext(last()).path("originNetworkId").textValue());
        client.bridge("origin", "destination", "0xabc", null, null, "bridge-1");
        assertEquals("/v1/api/bridge", last().path());
        assertFalse(plaintext(last()).has("senderAddress"));
        client.whitelistAddress("network-id", "0xabc");
        assertEquals("/v1/api/whitelist", last().path());
        client.getWhitelistedAddresses(true);
        assertEquals("includeNetwork=true", last().query());
    }

    @Test void moneyOperationRequiresIdempotencyKeyBeforeRetry() {
        status.set(503);
        CngnException error = assertThrows(CngnException.class, () ->
                client.redeemAsset(BigDecimal.ONE, "058", "0123456789", false, null));
        assertEquals(CngnException.Kind.SERVICE_UNAVAILABLE, error.kind());
        assertEquals(1, calls.size());
    }

    @Test void moneyOperationWithIdempotencyKeyRetriesSameRequest() throws Exception {
        client.close();
        byte[] seed = new byte[32]; seed[0] = 1;
        client = CngnClient.builder(API_KEY, ENCRYPTION_KEY, CngnCryptoTest.pem(seed))
                .baseUrl("http://127.0.0.1:" + server.getAddress().getPort() + "/v1/api")
                .maxRetries(1).build();
        transientFailures.set(1);
        client.redeemAsset(BigDecimal.ONE, "058", "0123456789", false, "redeem-once");
        assertEquals(2, calls.size());
        assertEquals("redeem-once", calls.get(0).idempotencyKey());
        assertEquals("redeem-once", calls.get(1).idempotencyKey());
        assertArrayEquals(calls.get(0).body(), calls.get(1).body());
    }

    @Test void rejectsEnvironmentMismatchAndInvalidAmounts() throws Exception {
        byte[] seed = new byte[32]; seed[0] = 1;
        assertEquals(CngnException.Kind.CONFIGURATION, assertThrows(CngnException.class, () ->
                CngnClient.builder(API_KEY, ENCRYPTION_KEY, CngnCryptoTest.pem(seed))
                        .environment("live").build()).kind());
        assertThrows(IllegalArgumentException.class, () ->
                client.withdraw(BigDecimal.ZERO, "0xabc", "network", false, null));
        assertThrows(IllegalArgumentException.class, () ->
                client.withdraw(BigDecimal.ONE, "0xabc", "network", false, " spaced "));
        assertThrows(IllegalArgumentException.class, () -> client.verifyWithdrawal("."));
        assertThrows(IllegalArgumentException.class, () -> client.verifyWithdrawal(".."));
        assertTrue(calls.isEmpty());
    }

    @Test void rejectsOversizedResponse() {
        responseOverride.set(new byte[10 * 1024 * 1024 + 1]);
        CngnException error = assertThrows(CngnException.class, client::getBalance);
        assertEquals(CngnException.Kind.API, error.kind());
        assertEquals(200, error.httpStatus());
    }

    @Test void transactionIteratorFetchesOnDemand() {
        var iterator = client.iterateTransactions(25).iterator();
        assertTrue(calls.isEmpty());
        assertFalse(iterator.hasNext());
        assertFalse(iterator.hasNext());
        assertEquals(1, calls.size());
        assertTrue(last().query().contains("limit=25"));
    }

    @Test void mapsExpandedNetworkBlockchainObject() {
        responseOverride.set("{\"status\":200,\"data\":[{\"id\":\"network-id\",\"name\":\"Base\",\"blockchain\":{\"name\":\"Ethereum\"}}]}"
                .getBytes(StandardCharsets.UTF_8));
        var networks = client.getNetworks(true);
        assertEquals(1, networks.size());
        assertEquals("Ethereum", networks.getFirst().blockchain().path("name").textValue());
        assertEquals("includeBlockchain=true", last().query());
    }
}
