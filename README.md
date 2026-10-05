# cngn-java-library

Java 25 LTS SDK for the [cNGN API](https://docs.cngn.co).

## Requirements

- Java 25 or newer and Maven 3.9 or newer.
- A `cngn_test_` or `cngn_live_` API key and the matching dashboard encryption key.
- An unencrypted OpenSSH Ed25519 private key whose public half is registered with cNGN. Generate one with `ssh-keygen -t ed25519`.

The client selects the API environment from the key prefix and rejects a conflicting explicit environment. The default API URL is `https://api.cngn.co/v1/api`. Custom URLs must use HTTPS; plain HTTP is accepted only for loopback integration tests. The private key stays inside the client process.

## Build

```sh
mvn test
```

To use the SDK from your local Maven repository, run `mvn install` and add:

```xml
<dependency>
    <groupId>io.github.wrappedcbdc-cngn</groupId>
    <artifactId>cngn-java-library</artifactId>
    <version>2.0.0-SNAPSHOT</version>
</dependency>
```

Run `mvn -Prelease-artifacts package` to build source and Javadoc JARs.

## Quickstart

```java
import co.cngn.sdk.CngnClient;
import java.nio.file.Files;
import java.nio.file.Path;

String privateKey = Files.readString(Path.of("/secure/path/cngn_ed25519"));
try (CngnClient cngn = CngnClient.builder(
        System.getenv("CNGN_API_KEY"),
        System.getenv("CNGN_ENCRYPTION_KEY"),
        privateKey).build()) {
    System.out.println(cngn.getBalance());
    System.out.println(cngn.getTransactions(1, 20));
}
```

Amounts use `BigDecimal` to preserve decimal precision. Responses use typed records in `ApiModels`. Network operations take IDs returned by `getNetworks()`.

## API operations

`CngnClient` provides `getBalance`, `getTransactions`, `iterateTransactions`, `getNetworks`, `getVirtualAccounts`, `createTemporaryVirtualAccount`, `redeemAsset`, `verifyBankAccount`, `getBanks`, `updateBankAccount`, `withdraw`, `verifyWithdrawal`, `getBridgeQuote`, `bridge`, `whitelistAddress`, and `getWhitelistedAddresses`.

Money operations accept an idempotency key. Reuse a unique key for retries of the same logical operation:

```java
import java.math.BigDecimal;

var result = cngn.redeemAsset(
        new BigDecimal("1000.00"), "058", "0123456789", false, "order-123-redeem");
```

The SDK retries transient network errors, 429s, and 5xx responses for reads. It retries a money operation only when that call has an idempotency key. A 429 waits 60 seconds before the next attempt. Configure request timeout and retry count through the builder.

API, authentication, IP allowlist, permission, validation, rate limit, network, encryption, and decryption failures throw `CngnException`. Inspect `kind()`, `httpStatus()`, and `response()` for details.

## Webhooks

Capture the exact raw body, verify its `X-cNGN-Signature` header, then parse it:

```java
import co.cngn.sdk.Webhooks;

if (!Webhooks.verify(rawBody, signatureHeader, signingSecret)) {
    throw new SecurityException("Invalid cNGN webhook signature");
}
Webhooks.Event event = Webhooks.parse(rawBody);
```

`Webhooks.verify` expects `sha256=<hex>` and uses a constant time digest comparison. Your application must handle duplicate webhook deliveries safely.

## Local wallet helpers

`Wallets.generate(network)` makes a fresh 12 word BIP-39 wallet. `Wallets.fromMnemonic(network, phrase)` restores it. Supported networks are Ethereum, Base, BSC, Polygon, Tron, Solana, Stellar, and XBN. EVM networks use `m/44'/60'/0'/0/0`, Tron uses `m/44'/195'/0'/0/0`, Solana uses `m/44'/501'/0'/0'`, Stellar uses `m/44'/148'/0'`, and XBN uses `m/44'/703'/0'`.

```java
import co.cngn.sdk.wallet.Wallets;

var wallet = Wallets.generate(Wallets.Network.BASE);
boolean valid = Wallets.validateAddress(Wallets.Network.BASE, wallet.address());
```

The returned wallet contains its mnemonic and private key. Store them in a dedicated secret manager; its `toString()` redacts them.
