# CNGN Java Library

A Java SDK for seamless integration with the cNGN API, enabling digital asset operations including balance checks, withdraw, virtual account management, and wallet operations.

## Table of Contents

1. [Installation](#installation)
    - [Maven](#maven)
    - [Gradle](#gradle)
2. [Quick Start](#quick-start)
3. [Features](#features)
    - [Asset Management](#asset-management)
    - [Wallet Operations](#wallet-operations)
4. [Supported Networks](#supported-networks)
    - [EVM Compatible](#evm-compatible)
    - [Other Chains](#other-chains)
5. [API Reference](#api-reference)
    - [CNGNManager](#cngnmanager)
        - [Balance Operations](#balance-operations)
        - [Transaction History](#view-transaction-history)
        - [Withdrawal](#balance-operations-withdrawal)
        - [Virtual Account Creation](#create-virtual-account)
        - [Asset Redemption](#redeem-assets)
        - [Business Details Update](#update-business-details)
        - [Bank Information](#get-supported-banks)
    - [WalletManager](#walletmanager)
6. [Address Formats](#address-formats)
7. [Error Handling](#error-handling)
8. [Security](#security)
9. [Testing](#testing)
10. [Support](#support)
11. [License](#license)

## Installation

### Maven
```xml
<dependency>
    <groupId>com.cngn</groupId>
    <artifactId>cngn-java-library</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle
```groovy
implementation 'com.cngn:cngn-java-library:1.0.0'
```

## Quick Start

```java
import com.cngn.CNGNManager;
import com.cngn.models.Secrets;

// Initialize the SDK
Secrets secrets = new Secrets(
        "your-api-key",
        "your-private-key",
        "your-encryption-key"
);
        CNGNManager manager = new CNGNManager(secrets);
```

## Features

### Asset Management
- Balance inquiries
- Transaction history
- Withdraw
- Asset redemption
- Virtual account creation
- Update external account details

### Wallet Operations
- Multi-chain wallet generation

## Supported Networks

### EVM Compatible
- Binance Smart Chain (BSC)
- Ethereum (ETH)
- Polygon (MATIC)
- Base
- Asset Chain (ATC)

### Other Chains
- XBN (Bantu)
- Tron (TRX)

## API Reference

### CNGNManager

#### Balance Operations
```java
JSONArray balance = manager.getBalance();
```

#### View transaction history
```java
JSONArray transactions = manager.getTransactionHistory();
```

#### Balance Operations Withdrawal
```java
WithdrawParams params = new WithdrawParams(
        100.0,              // amount
        "0x123...def",     // destination address
        Network.BSC        // destination network
);
JSONObject result = manager.withdraw(params);
```

#### Create virtual account
```java
CreateVirtualAccount accountParams = new CreateVirtualAccount(
        "korapay",    // default provider
        "011"         // bank code
);
JSONObject account = manager.createVirtualAccount(accountParams);
```

#### Redeem assets
```java
RedeemAssetParams redeemParams = new RedeemAssetParams(
        1000.0,          // amount
        "123",           // bank code
        "1234567890",    // account number
        true             // save details flag
);
JSONObject redemption = manager.redeemAssets(redeemParams);
```

#### Update business details
```java
UpdateExternalAccountParams updateParams = new UpdateExternalAccountParams(
        "Bank Name",
        "Account Name",
        "1234567890"    // account number
);

// Add wallet addresses
updateParams.addWalletAddress("bsc", "0x123...def");
updateParams.addWalletAddress("xbn", "G123...XYZ");

JSONObject result = manager.updateExternalAccounts(updateParams);
```

#### Get supported banks
```java
JSONArray banks = manager.getBanks();
```

### WalletManager

```java
// Generate wallet for any supported network
JSONObject wallet = WalletManager.generateWalletAddress(Network.BSC);

// Wallet response structure
{
        "address": "0x71C7...",    // Public address
        "network": "BSC",          // Network identifier
        "mnemonic": "word1 word2...",  // Recovery phrase
        "privateKey": "0x..."      // Private key
        }
```

## Address Formats
- EVM chains (BSC, ETH, MATIC, Base, ATC): `0x` prefix
- Bantu/XBN: `G` prefix (Stellar format)
- Tron: `T` prefix

## Error Handling

The library uses `CNGNException` for error management:

```java
try {
JSONArray balance = manager.getBalance();
} catch (CNGNException e) {
        System.err.println("Error code: " + e.getCode());
        System.err.println("Message: " + e.getMessage());
        }
```

## Security

- Uses AES encryption for request payloads
- Implements Ed25519 decryption for responses
- Requires secure management of API credentials

## Testing

Run the test suite:

```bash
mvn test
```

Test coverage includes:
- API endpoint integration
- Encryption/decryption
- Error scenarios
- Wallet generation
- Input validation

## Support

- Issues: [GitHub Issues](https://github.com/wrappedcbdc/cngn-java-library/issues)
- Documentation: [Wiki](https://github.com/wrappedcbdc/cngn-java-library/wiki)

## License

Released under [ISC License](LICENSE)