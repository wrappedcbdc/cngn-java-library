# cNGNManager Java Library

`cNGNManager` is a Java library for interacting with the CNGN API. It provides an intuitive interface for operations like checking balances, chain swaps, depositing for redemption, creating virtual accounts, generating wallet addresses, and more.

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

To include `cNGNManager` in your Java project:
1. Download or clone the repository.
2. Add the `.jar` file (if provided) to your project's build path, or add the source files directly to your project.

## Usage

First, import the `CNGNManager` class and other necessary components:

`java
import controller.ServiceController;
import dao.MerchantService;
import util.Network;
import org.json.JSONArray;
import org.json.JSONObject`


Initialize with credentials 
Add the following params
api-key
private-key
encryption-key

`CNGNManager cngnManager = new CNGNManger("api-key", "private-key", "encryption-key");`

Available Methods


Get Balance

`JSONArray balance = cngnManager.getBalance();
System.out.println("Balance: " + balance);`

Get Transaction History

`JSONArray transactions = cngnManager.getTransactionHistory();
System.out.println("Transaction History: " + transactions);`


Swap Between Chains

`JSONObject swapResult = cngnManager.swap(100, "0x1234...", Network.BSC);
System.out.println("Swap Result: " + swapResult);`

Deposit for Redemption

`JSONObject depositResult = cngnManager.deposit(1000, "Example Bank", "1234567890");
System.out.println("Deposit Result: " + depositResult);`


Create Virtual Account

`JSONObject virtualAccount = cngnManager.createVirtualAccount("korapay");
System.out.println("Virtual Account: " + virtualAccount);`

Whitelist Address

`JSONObject whitelistResult = cngnManager.whiteList("0x1234...", "Example Bank", "1234567890");
System.out.println("Whitelist Result: " + whitelistResult);`

Generate Wallet Address

`JSONObject walletAddress = cngnManager.generateWalletAddress(Network.ETH);
System.out.println("Wallet Address: " + walletAddress);`


Testing
To test the cNGNManager library, create a Java application with a main method, as shown in the main class provided in the code example. Running the program will print the results for each API call.

Error Handling
The library handles errors by returning structured JSON objects with error messages for failed requests. API errors are caught and returned as JSON objects containing descriptive messages.

Types
This library uses Java objects for API parameters and return types, such as JSONObject and JSONArray for JSON data, and Network enum for specifying networks.

Security
This library employs AES encryption for request payloads and Ed25519 decryption for response data. Keep your encryptionKey and privateKey secure to maintain API security.

Contributing
Contributions, issues, and feature requests are welcome. Please check the issues page on GitHub to see where you can help.

Support
For questions or support, please open an issue on the GitHub repository.
