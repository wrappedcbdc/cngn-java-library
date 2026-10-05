package co.cngn.sdk;

import co.cngn.sdk.ApiModels.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

/** Thread-safe client for the cNGN v1 API. Network IDs are returned by {@link #getNetworks()}. */
public final class CngnClient implements AutoCloseable {
    public static final String DEFAULT_BASE_URL = "https://api.cngn.co/v1/api";
    private static final int MAX_RESPONSE_BYTES = 10 * 1024 * 1024;
    private static final TypeReference<List<Balance>> BALANCES = new TypeReference<>() {};
    private static final TypeReference<List<Network>> NETWORKS = new TypeReference<>() {};
    private static final TypeReference<List<VirtualAccount>> ACCOUNTS = new TypeReference<>() {};
    private static final TypeReference<List<Bank>> BANKS = new TypeReference<>() {};
    private static final TypeReference<List<WhitelistEntry>> WHITELIST = new TypeReference<>() {};

    private final String apiKey;
    private final byte[] aesKey;
    private final byte[] edSeed;
    private final URI baseUrl;
    private final HttpClient http;
    private final boolean ownsHttp;
    private final AtomicBoolean closed = new AtomicBoolean();
    private final Duration timeout;
    private final int maxRetries;
    private final ObjectMapper json = new ObjectMapper()
            .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);

    private CngnClient(Builder builder) {
        apiKey = required(builder.apiKey, "apiKey");
        String inferred = apiKey.startsWith("cngn_test_") ? "test"
                : apiKey.startsWith("cngn_live_") ? "live" : null;
        if (inferred == null) throw new CngnException(CngnException.Kind.CONFIGURATION,
                "API key must start with cngn_test_ or cngn_live_");
        if (builder.environment != null && !builder.environment.equals(inferred)) {
            throw new CngnException(CngnException.Kind.CONFIGURATION,
                    "API key and selected environment do not match");
        }
        String url = builder.baseUrl == null ? DEFAULT_BASE_URL : builder.baseUrl;
        baseUrl = URI.create(url.replaceAll("/+$", ""));
        if (!"https".equalsIgnoreCase(baseUrl.getScheme())
                && !("http".equalsIgnoreCase(baseUrl.getScheme())
                && ("localhost".equalsIgnoreCase(baseUrl.getHost())
                || "127.0.0.1".equals(baseUrl.getHost())))) {
            throw new CngnException(CngnException.Kind.CONFIGURATION, "Base URL must use HTTPS");
        }
        if (baseUrl.getRawQuery() != null || baseUrl.getRawFragment() != null || baseUrl.getUserInfo() != null) {
            throw new CngnException(CngnException.Kind.CONFIGURATION, "Base URL must not contain credentials, query or fragment");
        }
        timeout = builder.timeout;
        maxRetries = builder.maxRetries;
        edSeed = CngnCrypto.parseOpenSshSeed(required(builder.privateKey, "privateKey"));
        aesKey = CngnCrypto.aesKey(required(builder.encryptionKey, "encryptionKey"));
        ownsHttp = builder.http == null;
        http = builder.http == null ? HttpClient.newBuilder()
                .connectTimeout(timeout).followRedirects(HttpClient.Redirect.NEVER).build() : builder.http;
    }

    public static Builder builder(String apiKey, String encryptionKey, String privateKey) {
        return new Builder(apiKey, encryptionKey, privateKey);
    }

    public static final class Builder {
        private final String apiKey;
        private final String encryptionKey;
        private final String privateKey;
        private String environment;
        private String baseUrl;
        private Duration timeout = Duration.ofSeconds(30);
        private int maxRetries = 3;
        private HttpClient http;

        private Builder(String apiKey, String encryptionKey, String privateKey) {
            this.apiKey = apiKey; this.encryptionKey = encryptionKey; this.privateKey = privateKey;
        }
        public Builder environment(String value) { environment = value; return this; }
        public Builder baseUrl(String value) { baseUrl = value; return this; }
        public Builder timeout(Duration value) {
            if (value == null || value.isNegative() || value.isZero()) throw new IllegalArgumentException("timeout must be positive");
            timeout = value; return this;
        }
        public Builder maxRetries(int value) {
            if (value < 0 || value > 10) throw new IllegalArgumentException("maxRetries must be between 0 and 10");
            maxRetries = value; return this;
        }
        /** Primarily useful for supplying a test transport. */
        public Builder httpClient(HttpClient value) { http = Objects.requireNonNull(value); return this; }
        public CngnClient build() { return new CngnClient(this); }
    }

    private static String required(String value, String name) {
        if (value == null || value.isBlank()) throw new CngnException(CngnException.Kind.CONFIGURATION,
                name + " is required");
        return value;
    }

    private static String segment(String value) {
        return URLEncoder.encode(required(value, "path segment"), StandardCharsets.UTF_8).replace("+", "%20");
    }

    private static void amount(BigDecimal value) {
        if (value == null || value.signum() <= 0) throw new IllegalArgumentException("amount must be positive");
    }

    public List<Balance> getBalance() { return call("GET", "/balance", null, null, BALANCES, true, null); }
    public TransactionPage getTransactions(int page, int limit) {
        if (page < 1 || limit < 1) throw new IllegalArgumentException("page and limit must be positive");
        return call("GET", "/transactions", Map.of("page", page, "limit", limit), null,
                new TypeReference<>() {}, true, null);
    }
    public Iterable<Transaction> iterateTransactions() { return iterateTransactions(100); }
    public Iterable<Transaction> iterateTransactions(int limit) {
        if (limit < 1) throw new IllegalArgumentException("limit must be positive");
        return () -> new Iterator<>() {
            private int page = 1;
            private List<Transaction> entries = List.of();
            private int index;
            private boolean finished;

            @Override public boolean hasNext() {
                while (index >= entries.size() && !finished) {
                    TransactionPage batch = getTransactions(page++, limit);
                    entries = batch.data();
                    index = 0;
                    finished = batch.pagination().lastPage() || entries.isEmpty();
                }
                return index < entries.size();
            }

            @Override public Transaction next() {
                if (!hasNext()) throw new NoSuchElementException();
                return entries.get(index++);
            }
        };
    }
    public List<Network> getNetworks() { return getNetworks(false); }
    public List<Network> getNetworks(boolean includeBlockchain) {
        return call("GET", "/networks", includeBlockchain ? Map.of("includeBlockchain", "true") : null,
                null, NETWORKS, true, null);
    }
    public List<VirtualAccount> getVirtualAccounts() {
        return call("GET", "/virtual-account", null, null, ACCOUNTS, true, null);
    }
    public TemporaryAccount createTemporaryVirtualAccount(BigDecimal value, String customerEmail,
            String customerName, String accountName, String narration, String idempotencyKey) {
        amount(value);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("amount", value);
        body.put("customer", Map.of("email", required(customerEmail, "customerEmail"),
                "name", required(customerName, "customerName")));
        body.put("accountName", required(accountName, "accountName"));
        if (narration != null) body.put("narration", narration);
        return call("POST", "/virtual-account/temporary", null, body,
                new TypeReference<>() {}, false, idempotencyKey);
    }
    public TransferResult redeemAsset(BigDecimal value, String bankCode, String accountNumber,
            boolean saveDetails, String idempotencyKey) {
        amount(value);
        return call("POST", "/redeemAsset", null, Map.of("amount", value,
                "bankCode", required(bankCode, "bankCode"), "accountNumber", required(accountNumber, "accountNumber"),
                "saveDetails", saveDetails), new TypeReference<>() {}, false, idempotencyKey);
    }
    public AccountVerification verifyBankAccount(String bankCode, String accountNumber) {
        return call("POST", "/account/verify", null, Map.of("bankCode", required(bankCode, "bankCode"),
                "accountNumber", required(accountNumber, "accountNumber")), new TypeReference<>() {}, true, null);
    }
    public List<Bank> getBanks() { return call("GET", "/banks", null, null, BANKS, true, null); }
    public JsonNode updateBankAccount(String bankName, String accountName, String accountNumber) {
        return call("PUT", "/bank-account", null, Map.of("bankName", required(bankName, "bankName"),
                "bankAccountName", required(accountName, "accountName"),
                "bankAccountNumber", required(accountNumber, "accountNumber")),
                new TypeReference<>() {}, false, null);
    }
    public TransferResult withdraw(BigDecimal value, String address, String networkId,
            boolean shouldSaveAddress, String idempotencyKey) {
        amount(value);
        return call("POST", "/withdraw", null, Map.of("amount", value,
                "address", required(address, "address"), "networkId", required(networkId, "networkId"),
                "shouldSaveAddress", shouldSaveAddress), new TypeReference<>() {}, false, idempotencyKey);
    }
    public Transaction verifyWithdrawal(String transactionReference) {
        if (".".equals(transactionReference) || "..".equals(transactionReference)) {
            throw new IllegalArgumentException("Invalid withdrawal reference");
        }
        return call("GET", "/withdraw/verify/" + segment(transactionReference), null, null,
                new TypeReference<>() {}, true, null);
    }
    public BridgeQuote getBridgeQuote(BigDecimal value, String originNetworkId,
            String destinationNetworkId, String destinationAddress) {
        amount(value);
        return call("POST", "/bridge-quote", null, Map.of("amount", value,
                "originNetworkId", required(originNetworkId, "originNetworkId"),
                "destinationNetworkId", required(destinationNetworkId, "destinationNetworkId"),
                "destinationAddress", required(destinationAddress, "destinationAddress")),
                new TypeReference<>() {}, true, null);
    }
    public BridgeResult bridge(String originNetworkId, String destinationNetworkId,
            String destinationAddress, String senderAddress, String callbackUrl, String idempotencyKey) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("originNetworkId", required(originNetworkId, "originNetworkId"));
        body.put("destinationNetworkId", required(destinationNetworkId, "destinationNetworkId"));
        body.put("destinationAddress", required(destinationAddress, "destinationAddress"));
        if (senderAddress != null) body.put("senderAddress", senderAddress);
        if (callbackUrl != null) body.put("callbackUrl", callbackUrl);
        return call("POST", "/bridge", null, body, new TypeReference<>() {}, false, idempotencyKey);
    }
    public JsonNode whitelistAddress(String networkId, String address) {
        return call("POST", "/whitelist", null, Map.of("networkId", required(networkId, "networkId"),
                "address", required(address, "address")), new TypeReference<>() {}, false, null);
    }
    public List<WhitelistEntry> getWhitelistedAddresses(boolean includeNetwork) {
        return call("GET", "/whitelisted", includeNetwork ? Map.of("includeNetwork", "true") : null,
                null, WHITELIST, true, null);
    }

    private <T> T call(String method, String path, Map<String, ?> query, Object body,
                       TypeReference<T> type, boolean safeToRetry, String idempotencyKey) {
        try {
            return callAsync(method, path, query, body, type, safeToRetry, idempotencyKey).join();
        } catch (CompletionException e) {
            if (e.getCause() instanceof RuntimeException runtime) throw runtime;
            throw e;
        }
    }

    private <T> CompletableFuture<T> callAsync(String method, String path, Map<String, ?> query,
            Object body, TypeReference<T> type, boolean safeToRetry, String idempotencyKey) {
        boolean hasKey = idempotencyKey != null && !idempotencyKey.isBlank();
        if (idempotencyKey != null && (!hasKey || !idempotencyKey.equals(idempotencyKey.trim()))) {
            throw new IllegalArgumentException("idempotencyKey must be nonblank without surrounding whitespace");
        }
        HttpRequest request = request(method, path, query, body, idempotencyKey);
        return attempt(request, type, safeToRetry || hasKey, 0);
    }

    private HttpRequest request(String method, String path, Map<String, ?> query, Object body, String key) {
        if (closed.get()) throw new IllegalStateException("CngnClient is closed");
        StringBuilder url = new StringBuilder(baseUrl.toString()).append(path);
        if (query != null && !query.isEmpty()) {
            url.append('?');
            query.forEach((name, value) -> {
                if (url.charAt(url.length() - 1) != '?') url.append('&');
                url.append(segment(name)).append('=').append(segment(String.valueOf(value)));
            });
        }
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url.toString()))
                .timeout(timeout).header("Authorization", "Bearer " + apiKey)
                .header("Accept", "application/json").header("Content-Type", "application/json");
        if (key != null) builder.header("Idempotency-Key", key);
        try {
            if (body == null) return builder.GET().build();
            byte[] plain = json.writeValueAsBytes(body);
            try {
                byte[] wire = json.writeValueAsBytes(CngnCrypto.encrypt(plain, aesKey));
                return builder.method(method, HttpRequest.BodyPublishers.ofByteArray(wire)).build();
            } finally {
                Arrays.fill(plain, (byte) 0);
            }
        } catch (JsonProcessingException e) {
            throw new CngnException(CngnException.Kind.ENCRYPTION, "Could not serialize request", e);
        }
    }

    private <T> CompletableFuture<T> attempt(HttpRequest request, TypeReference<T> type,
                                               boolean retryable, int retry) {
        return http.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                .<T>handle((response, failure) -> {
                    if (failure != null) throw new CompletionException(new CngnException(
                            CngnException.Kind.NETWORK, "cNGN transport failed", failure));
                    return parse(response, type);
                }).handle((value, failure) -> {
                    if (failure == null) return CompletableFuture.completedFuture(value);
                    Throwable cause = failure instanceof CompletionException ? failure.getCause() : failure;
                    boolean transientError = cause instanceof CngnException error
                            && (error.kind() == CngnException.Kind.NETWORK
                            || error.kind() == CngnException.Kind.RATE_LIMIT
                            || error.kind() == CngnException.Kind.SERVICE_UNAVAILABLE);
                    if (!retryable || !transientError || retry >= maxRetries) {
                        return CompletableFuture.<T>failedFuture(cause);
                    }
                    long delay = cause instanceof CngnException error && error.kind() == CngnException.Kind.RATE_LIMIT
                            ? 60 : Math.min(1L << retry, 30);
                    return CompletableFuture.supplyAsync(() -> true,
                            CompletableFuture.delayedExecutor(delay, TimeUnit.SECONDS))
                            .thenCompose(ignored -> attempt(request, type, retryable, retry + 1));
                }).thenCompose(future -> future);
    }

    private <T> T parse(HttpResponse<InputStream> response, TypeReference<T> type) {
        byte[] body;
        try (InputStream stream = response.body()) {
            body = stream.readNBytes(MAX_RESPONSE_BYTES + 1);
        } catch (IOException e) {
            throw new CngnException(CngnException.Kind.NETWORK, "Could not read cNGN response", e);
        }
        if (body.length > MAX_RESPONSE_BYTES) {
            throw new CngnException(CngnException.Kind.API, "cNGN response exceeds size limit",
                    response.statusCode(), null);
        }
        JsonNode payload;
        try {
            payload = json.readTree(body);
        } catch (IOException e) {
            CngnException.Kind kind = response.statusCode() == 429 ? CngnException.Kind.RATE_LIMIT
                    : response.statusCode() >= 500 ? CngnException.Kind.SERVICE_UNAVAILABLE
                    : CngnException.Kind.API;
            throw new CngnException(kind, "cNGN returned invalid JSON",
                    response.statusCode(), null, e);
        }
        if (payload == null || !payload.isObject()) {
            throw new CngnException(CngnException.Kind.API, "cNGN returned an invalid response envelope",
                    response.statusCode(), payload);
        }
        int status = payload.path("status").asInt(response.statusCode());
        if (response.statusCode() < 200 || response.statusCode() >= 300 || status != 200
                || payload.path("status").isBoolean() && !payload.path("status").booleanValue()) {
            throw mapError(response.statusCode(), payload);
        }
        JsonNode data = payload.get("data");
        if (data != null && data.isTextual()) data = CngnCrypto.decryptResponse(data.textValue(), edSeed, json);
        if (data == null || data.isNull()) {
            if (type.getType().getTypeName().startsWith("java.util.List")) data = json.createArrayNode();
            else data = json.createObjectNode();
        }
        try {
            return json.convertValue(data, type);
        } catch (IllegalArgumentException e) {
            throw new CngnException(CngnException.Kind.API, "Unexpected cNGN response data", response.statusCode(), payload, e);
        }
    }

    private CngnException mapError(int status, JsonNode payload) {
        String message = payload.path("message").asText("HTTP " + status);
        String lower = message.toLowerCase(java.util.Locale.ROOT);
        CngnException.Kind kind;
        if (status == 429 || lower.contains("too many requests")) kind = CngnException.Kind.RATE_LIMIT;
        else if (lower.contains("ip address") && lower.contains("whitelist")) kind = CngnException.Kind.IP_ALLOWLIST;
        else if (lower.contains("permission denied")) kind = CngnException.Kind.PERMISSION;
        else if (status == 401 || lower.contains("token") || lower.contains("merchant not found")
                || lower.contains("ssh key found")) kind = CngnException.Kind.AUTHENTICATION;
        else if (lower.contains("transaction not found")) kind = CngnException.Kind.NOT_FOUND;
        else if (status == 400) kind = CngnException.Kind.VALIDATION;
        else if (status >= 500) kind = CngnException.Kind.SERVICE_UNAVAILABLE;
        else kind = CngnException.Kind.API;
        return new CngnException(kind, message, status, payload);
    }

    @Override public void close() {
        if (closed.compareAndSet(false, true)) {
            if (ownsHttp) http.close();
            Arrays.fill(aesKey, (byte) 0);
            Arrays.fill(edSeed, (byte) 0);
        }
    }
}
