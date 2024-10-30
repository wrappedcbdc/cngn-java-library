import controller.ServiceController;
import dao.Secrets;
import dao.params.*;
import org.json.JSONArray;
import org.json.JSONObject;
import util.Network;

import java.util.HashMap;
import java.util.Map;

import static util.Constants.*;

public class CNGNManger {



    private Secrets secrets;

    public CNGNManger(String apiKey, String privateKey, String encryptionKey) {
        secrets = new Secrets(apiKey, privateKey, encryptionKey);
        return;
    }

    public JSONArray getBalance() {
        return ServiceController.makeCalls(GET_BALANCE, secrets);
    }

    public JSONArray getTransactionHistory() {
        return ServiceController.makeCalls(TRANSACTIONS, secrets);
    }

    public JSONArray getBanks() {
        return ServiceController.makeCalls(GET_BANKS, secrets);
    }

    public JSONObject swap(SwapParams swapParams) {
        JSONObject payload = new JSONObject();
        payload.put("amount", swapParams.getAmount());
        payload.put("address", swapParams.getAddress());
        payload.put("network", swapParams.getNetwork().toString().toLowerCase());
        return ServiceController.makeCalls(SWAP, secrets, payload);
    }

    public JSONObject deposit(DepositParams depositParams) {
        JSONObject payload = new JSONObject();
        payload.put("amount", depositParams.getAmount());
        payload.put("bank", depositParams.getBank());
        payload.put("accountNumber", depositParams.getAccountNumber());
        return ServiceController.makeCalls(DEPOSIT, secrets, payload);
    }

    public JSONObject createVirtualAccount(MintParams mintParams) {
        JSONObject payload = new JSONObject();
        payload.put("provider", mintParams.getProvider());
        payload.put("bank_code", mintParams.getBankCode());
        return ServiceController.makeCalls(CREATE_VIRTUAL_ACCOUNT, secrets, payload);
    }

    public JSONObject whiteList(WhiteListAddressParams whiteListAddressParams) {
        JSONObject payload = new JSONObject();
        payload.put("bscAddress", whiteListAddressParams.getBscAddress());
        payload.put("bankName", whiteListAddressParams.getBankName());
        payload.put("bankAccountNumber", whiteListAddressParams.getBankAccountNumber());
        return ServiceController.makeCalls(WHITELIST_ADDRESS, secrets, payload);
    }

    public JSONObject redeemAssets(RedeemAssetParams redeemAssetParams) {
        JSONObject payload = new JSONObject();
        payload.put("amount", redeemAssetParams.getAmount());
        payload.put("bankCode", redeemAssetParams.getBankCode());
        payload.put("accountNumber", redeemAssetParams.getAccountNumber());
        payload.put("saveDetails", redeemAssetParams.getSaveDetails());
        return ServiceController.makeCalls(REDEEM_ASSETS, secrets, payload);
    }

    public JSONObject updateExternalAccounts(UpdateExternalAccountParams updateExternalAccountParams) {
        Map<String, Object> payload = new HashMap<>();

        Map<String, String> walletAddressMap = new HashMap<>();
        walletAddressMap.put("bscAddress", updateExternalAccountParams.getWalletAddress().getAddress());
        // Add other chain addresses if needed

        Map<String, String> bankDetailsMap = new HashMap<>();
        bankDetailsMap.put("bankName", updateExternalAccountParams.getBankDetails().getBankName());
        bankDetailsMap.put("bankAccountName", updateExternalAccountParams.getBankDetails().getBankAccountName());
        bankDetailsMap.put("bankAccountNumber", updateExternalAccountParams.getBankDetails().getBankAccountNumber());

        payload.put("walletAddress", walletAddressMap);
        payload.put("bankDetails", bankDetailsMap);
        return ServiceController.makeCalls(UPDATE_EXTERNAL_ACCOUNTS, secrets, payload);
    }



    public static void main(String[] args) {
        System.out.println("----------------------BEGIN TEST-------------------------");
        CNGNManger cngnManger = new CNGNManger("api-key","private-key", "encryption-key");

        System.out.println("----------------------BALANCE-------------------------");
        System.out.println("Fetch Balance : " + cngnManger.getBalance());

        System.out.println("----------------------TRANSACTION-------------------------");
        System.out.println("Fetch Transaction History : " + cngnManger.getTransactionHistory());

        System.out.println("----------------------WHITELIST------------------------");
        WhiteListAddressParams whiteListAddressParams = new WhiteListAddressParams(
                "0x3d8e27756d784274C3C4CfeBCdFb2C096eE3cD0b",
                "Example Bank",
                "1234567890");
        System.out.println("WhiteList Address : " + cngnManger.whiteList(whiteListAddressParams));

        System.out.println("----------------------SWAP-------------------------");
        SwapParams swapParams = new SwapParams(
                1000,
                "0x3d8e27756d784274C3C4CfeBCdFb2C096eE3cD0b",
                Network.ETH);
        System.out.println("Swap : " + cngnManger.swap(swapParams));

        System.out.println("----------------------DEPOSIT-------------------------");
        DepositParams depositParams = new DepositParams(
                1000,
                "Example Bank",
                "1234567890");
        System.out.println("Deposit : " + cngnManger.deposit(depositParams));


        System.out.println("----------------------CREATE VIRTUAL ACCOUNT-------------------------");
        MintParams mintParams = new MintParams(
                "korapay",
                "123"
        );
        System.out.println("Create Virtual Account : " + cngnManger.createVirtualAccount(mintParams));


        System.out.println("----------------------REDEEM ASSETS-------------------------");
        RedeemAssetParams redeemAssetParams = new RedeemAssetParams(
                1000,
                "123",
                "1234567890",
                true
        );
        System.out.println("Redeem Assets : " + cngnManger.redeemAssets(redeemAssetParams));

        System.out.println("----------------------UPDATE EXTERNAL ACCOUNTS-------------------------");
        UpdateExternalAccountParams.WalletAddress walletAddress = new UpdateExternalAccountParams.WalletAddress("0x3d8e27756d784274C3C4CfeBCdFb2C096eE3cD0b");
        UpdateExternalAccountParams.BankDetails bankDetails = new UpdateExternalAccountParams.BankDetails("Test Bank", "Test Account", "1234567890");

        UpdateExternalAccountParams params = new UpdateExternalAccountParams(walletAddress, bankDetails);
        System.out.println("Update External Accounts " + cngnManger.updateExternalAccounts(params));


        System.out.println("----------------------GET BANKS-------------------------");
        System.out.println("Get Banks : " + cngnManger.getBanks());
    }
}

