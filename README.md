# cngn-java-library

cngn-java-library is a Java library for interacting with the cNGN API. It provides a simple interface for various operations such as checking balance, swapping between chains, depositing for redemption, creating virtual accounts, generating wallet addresses, and more.

## Table of Contents

- [Installation](#installation)
- [Usage](#usage)
- [Available Methods](#available-methods)
- [Testing](#testing)
- [Error Handling](#error-handling)
- [Types](#types)
- [Security](#security)
- [Contributing](#contributing)
- [Support](#support)
- [License](#license)

## Installation

To install cngn-java-library and its dependencies:

1. Add the following dependency to your `pom.xml` if using Maven:

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

First, import the necessary classes and types:

```java
import com.cngn.CNGNManager;
import com.cngn.WalletManager;
import com.cngn.models.Secrets;
import com.cngn.models.SwapParams;
import com.cngn.models.DepositParams;
import com.cngn.models.MintParams;
import com.cngn.models.WhiteListAddressParams;
import com.cngn.enums.Network;
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

## Available Methods

### CNGNManager Methods

#### Get Balance

```java
JSONArray balance = manager.getBalance();
System.out.println(balance);
```

#### Get Transaction History

```java
JSONArray transactions = manager.getTransactionHistory();
System.out.println(transactions);
```

#### Swap Between Chains

```java
SwapParams swapParams = new SwapParams(
    100,                    // amount
    "0x1234...",           // address
    Network.BSC            // network
);
JSONObject swapResult = manager.swapBetweenChains(swapParams);
System.out.println(swapResult);
```

#### Deposit for Redemption

```java
DepositParams depositParams = new DepositParams(
    1000,                  // amount
    "Example Bank",        // bank
    "1234567890"          // accountNumber
);
JSONObject depositResult = manager.depositForRedemption(depositParams);
System.out.println(depositResult);
```

#### Create Virtual Account

```java
MintParams mintParams = new MintParams("korapay");
JSONObject virtualAccount = manager.createVirtualAccount(mintParams);
System.out.println(virtualAccount);
```

#### Whitelist Address

```java
WhiteListAddressParams whitelistParams = new WhiteListAddressParams(
    "0x1234...",          // bscAddress
    "Example Bank",       // bankName
    "1234567890"         // bankAccountNumber
);
JSONObject whitelistResult = manager.whitelistAddress(whitelistParams);
System.out.println(whitelistResult);
```

### WalletManager Methods

#### Generate Wallet Address

```java
Network network = Network.ETH; // or any other supported network
JSONObject walletAddress = WalletManager.generateWalletAddress(network);
System.out.println(walletAddress);
```

This method generates a new wallet address for the specified network. The result includes the address, network, mnemonic phrase, and private key.

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
