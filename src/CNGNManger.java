import controller.ServiceController;
import dao.Secrets;
import dao.params.*;
import org.json.JSONArray;
import org.json.JSONObject;
import util.Network;

import static util.Constants.*;

public class CNGNManger {

    private final Secrets secrets;

    public CNGNManger(String apiKey, String privateKey, String encryptionKey) {
        secrets = new Secrets(apiKey, privateKey, encryptionKey);
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

    public JSONObject withdraw(WithdrawParams withdrawParams) {
        JSONObject payload = new JSONObject();
        payload.put("amount", withdrawParams.getAmount());
        payload.put("address", withdrawParams.getAddress());
        payload.put("network", withdrawParams.getNetwork().toString().toLowerCase());
        return ServiceController.makeCalls(WITHDRAW, secrets, payload);
    }

    public JSONObject createVirtualAccount(CreateVirtualAccount createVirtualAccount) {
        JSONObject payload = new JSONObject();
        payload.put("provider", createVirtualAccount.getProvider());
        payload.put("bank_code", createVirtualAccount.getBankCode());
        return ServiceController.makeCalls(CREATE_VIRTUAL_ACCOUNT, secrets, payload);
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
        return ServiceController.makeCalls(UPDATE_EXTERNAL_ACCOUNTS, secrets, updateExternalAccountParams.toJson());
    }

    public static void main(String[] args) {
        System.out.println("----------------------BEGIN TEST-------------------------");
        CNGNManger cngnManger = new CNGNManger(
                "api-key",
                "ssh secret key",
                "encryption key"
        );
        System.out.println("----------------------BALANCE-------------------------");
        System.out.println("Fetch Balance : " + cngnManger.getBalance());

        System.out.println("----------------------TRANSACTION-------------------------");
        System.out.println("Fetch Transaction History : " + cngnManger.getTransactionHistory());


        System.out.println("----------------------WITHDRAW-------------------------");
        WithdrawParams withdrawParams = new WithdrawParams(
                1000,
                "0x3d8e27756d784274C3C4CfeBCdFb2C096eE3cD0b",
                Network.ETH);
        System.out.println("Withdraw : " + cngnManger.withdraw(withdrawParams));


        System.out.println("----------------------CREATE VIRTUAL ACCOUNT-------------------------");
        CreateVirtualAccount createVirtualAccount = new CreateVirtualAccount(
                "korapay",
                "123"
        );
        System.out.println("Create Virtual Account : " + cngnManger.createVirtualAccount(createVirtualAccount));


        System.out.println("----------------------REDEEM ASSETS-------------------------");
        RedeemAssetParams redeemAssetParams = new RedeemAssetParams(
                1000,
                "123",
                "1234567890",
                true
        );
        System.out.println("Redeem Assets : " + cngnManger.redeemAssets(redeemAssetParams));

        System.out.println("----------------------UPDATE EXTERNAL ACCOUNTS-------------------------");
        UpdateExternalAccountParams updateExternalAccountParams = new UpdateExternalAccountParams(
                "Test Bank",
                "Example account",
                "1234567890"
        );
        updateExternalAccountParams.addWalletAddress("bscAddress","0x36febeeb4f12a011865d06837d1ddc18090f59b2");
        updateExternalAccountParams.addWalletAddress("xbnAddress","GASIKMSCK7IWMKHGD4QPP66ZADR5IX3YVAWXSXADJDVNULMC3LT3HZA6");
        //add other address

        System.out.println("Update External Accounts " + cngnManger.updateExternalAccounts(updateExternalAccountParams));

        System.out.println("----------------------GET BANKS-------------------------");
        System.out.println("Get Banks : " + cngnManger.getBanks());
    }
}
