# CNGN Java Library

A Java SDK for seamless integration with the cNGN API, enabling digital asset operations including balance checks, withdraw, virtual account management, and wallet operations.

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

// Make your first API call
JSONArray balance = manager.getBalance();
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
// Get balance across all chains
JSONArray balance = manager.getBalance();

// View transaction history
JSONArray transactions = manager.getTransactionHistory();
```

#### Asset Operations
```java
// Withdrawal
WithdrawParams params = new WithdrawParams(
    100.0,              // amount
    "0x123...def",     // destination address
    Network.BSC        // destination network
);
JSONObject result = manager.withdraw(params);

// Create virtual account
CreateVirtualAccount accountParams = new CreateVirtualAccount(
    "korapay",    // provider
    "123"         // bank code
);
JSONObject account = manager.createVirtualAccount(accountParams);

// Redeem assets
RedeemAssetParams redeemParams = new RedeemAssetParams(
    1000.0,          // amount
    "123",           // bank code
    "1234567890",    // account number
    true             // save details flag
);
JSONObject redemption = manager.redeemAssets(redeemParams);

// Update business details
UpdateExternalAccountParams updateParams = new UpdateExternalAccountParams(
        "Bank Name",
        "Account Name",
        "1234567890"    // account number
);

// Add wallet addresses
updateParams.addWalletAddress("bsc", "0x123...def");
updateParams.addWalletAddress("xbn", "G123...XYZ");

JSONObject result = manager.updateExternalAccounts(updateParams);


// Get supported banks
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