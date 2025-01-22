# cngn-java-library

cngn-java-library is a Java library for interacting with the cNGN API. It provides a simple interface for various operations such as checking balance, swapping between chains, depositing for redemption, creating virtual accounts, generating wallet addresses, and more.

## Table of Contents

- [Installation](#installation)
- [Usage](#usage)
- [Networks](#networks)
- [Available Methods](#available-methods)
  - [CNGNManager Methods](#cngnmanager-methods)
  - [WalletManager Methods](#walletmanager-methods)
- [Testing](#testing)
- [Error Handling](#error-handling)
- [Types](#types)
- [Security](#security)
- [Contributing](#contributing)
- [Support](#support)
- [License](#license)

## Installation

To install cngn-java-library and its dependencies:

Add the following dependency to your `pom.xml` if using Maven:

```xml
<dependency>
    <groupId>com.cngn</groupId>
    <artifactId>cngn-java-library</artifactId>
    <version>1.0.0</version>
</dependency>

```

Or in your `build.gradle` if using Gradle:

```groovy
implementation 'com.cngn:cngn-java-library:1.0.0'
```

## Usage

First, import the necessary classes:


```java
import com.cngn.CNGNManager;
import com.cngn.WalletManager;
import com.cngn.models.Secrets;
import com.cngn.models.SwapParams;
import com.cngn.models.DepositParams;
import com.cngn.models.MintParams;
import com.cngn.models.WhiteListAddressParams;
import com.cngn.enums.Network;
import org.json.JSONObject;
import org.json.JSONArray;
```

Then, create an instance of `CNGNManager` with your secrets:

```java
Secrets secrets = new Secrets(
    "your-api-key",
    "your-private-key",
    "your-encryption-key"
);

CNGNManager manager = new CNGNManager(secrets);

// Example: Get balance
JSONArray balance = manager.getBalance();
System.out.println(balance);
```

## Networks

The library supports multiple blockchain networks, grouped by their underlying chain technology:

### EVM (Ethereum Virtual Machine) Chains
- `Network.BSC` - Binance Smart Chain Mainnet
- `Network.ATC` - Asset Chain
- `Network.ETH` - Ethereum Mainnet
- `Network.MATIC` - Polygon (Previously Matic)
  - `Network.base` - Base

### Bantu (Stellar-based)
- `Network.XBN` - XBN Chain

### Tron
- `Network.TRX` - Tron Mainnet

Usage example with different chain types:
```java
// For EVM chain operations (BSC, ATC, ETH, MATIC)
SwapParams evmSwapParams = new SwapParams(
    100,                // amount
    "0x1234...",       // EVM-compatible address
    Network.BSC        // Network.base, Network.ATC, Network.ETH, Network.MATIC
);

// For Bantu (XBN) operations
SwapParams xbnSwapParams = new SwapParams(
    100,               // amount
    "G....",           // Stellar-compatible address
    Network.XBN
);

// For Tron operations
SwapParams tronSwapParams = new SwapParams(
    100,               // amount
    "T....",           // Tron address format
    Network.TRX
);
```

## Available Methods

### CNGNManager Methods

#### Get Balance

```java
JSONArray balance = manager.getBalance();
System.out.println(balance);
```

#### Get Transaction History

```java
 TransactionParams transactionParams = new TransactionParams(
                1, //page
                2  //limit
        );
System.out.println("Fetch Transaction History : " + cngnManager.getTransactionHistory(transactionParams));
```

#### Swap Between Chains

```java
SwapParams swapParams = new SwapParams(
    100,                // amount
    "0x1234...",       // address
    Network.BSC        // network
);
JSONObject swapResult = manager.swapBetweenChains(swapParams);
System.out.println(swapResult);
```


#### Create Virtual Account

```java
MintParams mintParams = new MintParams(
"korapay", //provider
"123"  //bankCode
);
JSONObject virtualAccount = manager.createVirtualAccount(mintParams);
System.out.println(virtualAccount);
```

#### Redeem Asset

```java
RedeemAssetParams redeemAssetParams = new RedeemAssetParams(
                1000,     //amount
                "123",      //bankCode
                "1234567890",   //accountNumber
                true //saveDetails
        );
System.out.println("Redeem Assets : " + cngnManger.redeemAssets(redeemAssetParams));
```

#### Update Business

```java
UpdateExternalAccountParams updateExternalAccountParams = new UpdateExternalAccountParams(
                "Test Bank",
                "Example account",
                "1234567890"
        );
        updateExternalAccountParams.addWalletAddress("bscAddress", "0x3d8e....");
System.out.println("Update External Accounts " + cngnManager.updateExternalAccounts(updateExternalAccountParams));
```


#### IWithdrawal
```java
IWithdrawParams withdrawParams = new IWithdrawParams(
                "1000", //amount
                "0x789...", //address
                Network.BSC, //network
                true //shouldSaveAddress
        );
System.out.println("Withdraw : " + cngnManager.withdraw(withdrawParams));

```

#### Verify Withdrawal Reference
```java
System.out.println("Withdraw : " + cngnManager.verifyWithdrawalReference(
                "123-456-789-789405" //Transaction Reference 
        ));
```


#### Fetch Banks

```java
System.out.println("Get Banks : " + cngnManger.getBanks());
```


### WalletManager Methods

#### Generate Wallet Address

The `generateWalletAddress` method creates a new wallet address for any supported network. Each network type returns addresses in its native format.

```java
// EVM Chains (returns 0x-prefixed addresses)
JSONObject bscWallet = WalletManager.generateWalletAddress(Network.BSC);    // BSC address
JSONObject ethWallet = WalletManager.generateWalletAddress(Network.ETH);    // Ethereum address
JSONObject maticWallet = WalletManager.generateWalletAddress(Network.MATIC); // Polygon address
JSONObject atcWallet = WalletManager.generateWalletAddress(Network.ATC);    // Asset Chain address

// Bantu/Stellar-based Chain (returns G-prefixed addresses)
JSONObject xbnWallet = WalletManager.generateWalletAddress(Network.XBN);    // XBN address

// Tron Chain (returns T-prefixed addresses)
JSONObject tronWallet = WalletManager.generateWalletAddress(Network.TRX);   // Tron address
```

The response format for each generated wallet:
```java
public class WalletResponse {
    private String address;     // The public address for the wallet
    private Network network;    // The network this wallet is for
    private String mnemonic;    // 12-word recovery phrase
    private String privateKey;  // Private key for the wallet
}
```

Example response:
```java
// EVM wallet response
{
    "address": "0x71C7656EC7ab88b098defB751B7401B5f6d8976F",
    "network": "ETH",
    "mnemonic": "width craft decide...",
    "privateKey": "0x..."
}

// XBN wallet response
{
    "address": "GBXYZABC...",
    "network": "XBN",
    "mnemonic": "width craft decide...",
    "privateKey": "S..."
}

// Tron wallet response
{
    "address": "TRxYZABC...",
    "network": "TRX",
    "mnemonic": "width craft decide...",
    "privateKey": "..."
}
```

**Note:** Each network type has its own address format:
- EVM chains (BSC, ETH, MATIC, ATC): `0x`-prefixed addresses
- Bantu/XBN: `G`-prefixed addresses (Stellar format)
- Tron: `T`-prefixed addresses

## Testing

This project uses JUnit for testing. To run the tests, follow these steps:

1. Run the test command:

   ```bash
   mvn test
   ```

   This will run all tests in the `src/test/java` directory.

### Test Structure

The tests are located in the `src/test/java` directory. They cover various aspects of the CNGNManager and WalletManager classes, including:

- API calls for different endpoints (GET and POST requests)
- Encryption and decryption of data
- Error handling for various scenarios
- Wallet address generation for different networks

## Error Handling

The library uses a custom error handling mechanism. All API errors are caught and thrown as `CNGNException` objects with descriptive messages.

## Types

The library includes Java classes and interfaces for all parameters and return types. All models are defined in the `com.cngn.models` package for easy reference and maintenance.

## Security

This library uses AES encryption for request payloads and Ed25519 decryption for response data. Ensure that your `encryptionKey` and `privateKey` are kept secure.

## Contributing

Contributions, issues, and feature requests are welcome. Feel free to check [issues page](https://github.com/wrappedcbdc/cngn-java-library/issues) if you want to contribute.

## Support

If you have any questions or need help using the library, please open an issue in the GitHub repository.

## License

[ISC](https://choosealicense.com/licenses/isc/)
