import controller.ServiceController;
import dao.MerchantService;
import org.json.JSONArray;
import org.json.JSONObject;
import util.Network;
import wallet.CryptoWallet;

import static util.Constants.*;

public class CNGNManger {
   
    private MerchantService merchantService;

    public CNGNManger(String apiKey, String privateKey, String encryptionKey) {
        merchantService = new MerchantService(apiKey, privateKey, encryptionKey);
        return;
    }

    public JSONArray getBalance() {
        return ServiceController.makeCallsGET(GET_BALANCE, merchantService);
    }

    public JSONArray getTransactionHistory() {
        return ServiceController.makeCallsGET(TRANSACTIONS, merchantService);
    }

    public JSONObject swap(int amount, String address, Network network) {
        JSONObject payload = new JSONObject();
        payload.put("amount", amount);
        payload.put("address", address);
        payload.put("network", network.toString().toLowerCase());
        return ServiceController.makeCallsPOST(SWAP, merchantService, payload);
    }

    public JSONObject deposit(int amount, String bank, String accountNumber) {
        JSONObject payload = new JSONObject();
        payload.put("amount", amount);
        payload.put("bank", bank);
        payload.put("accountNumber", accountNumber);
        return ServiceController.makeCallsPOST(DEPOSIT, merchantService, payload);
    }

    public JSONObject createVirtualAccount(String provider) {
        JSONObject payload = new JSONObject();
        payload.put("provider", provider);
        return ServiceController.makeCallsPOST(CREATE_VIRTUAL_ACCOUNT, merchantService, payload);
    }

    public JSONObject whiteList(String bscAddress, String bankName, String bankAccountNumber) {
        JSONObject payload = new JSONObject();
        payload.put("bscAddress", bscAddress);
        payload.put("bankName", bankName);
        payload.put("bankAccountNumber", bankAccountNumber);
        return ServiceController.makeCallsPOST(WHITELIST_ADDRESS, merchantService, payload);
    }

    public JSONObject generateWalletAddress(Network network) {
        return ServiceController.createWallet(network);
    }


    public static void main(String[] args) {
        System.out.println("----------------------BEGIN TEST-------------------------");
        CNGNManger cngnManger = new CNGNManger("api-key", "private-key", "encryption-key");
        System.out.println("----------------------BALANCE-------------------------");
        System.out.println("Fetch Balance : " + cngnManger.getBalance());

        System.out.println("----------------------TRANSACTION-------------------------");
        System.out.println("Fetch Transaction History : " + cngnManger.getTransactionHistory());

        System.out.println("----------------------WHITELIST------------------------");
        System.out.println("WhiteList Address : " + cngnManger.whiteList("0x96e1Cc6D03ADb0E0C5f073D1984d3AB23D329931", "Example Bank", "1234567890"));

        System.out.println("----------------------SWAP-------------------------");
        System.out.println("Swap : " + cngnManger.swap(1000, "0x96e1Cc6D03ADb0E0C5f073D1984d3AB23D329931", Network.ETH));

        System.out.println("----------------------DEPOSIT-------------------------");
        System.out.println("Deposit : " + cngnManger.deposit(1000, "Example Bank", "1234567890"));


        System.out.println("----------------------CREATE VIRTUAL ACCOUNT-------------------------");
        System.out.println("Create Virtual Account : " + cngnManger.createVirtualAccount("korapay"));

        System.out.println("----------------------GENERATE WALLET-------------------------");
        System.out.println("Generate Wallet Address : " + cngnManger.generateWalletAddress(Network.ETH));

        System.out.println("----------------------END TEST-------------------------");
    }
}

