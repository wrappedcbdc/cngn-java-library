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

    public JSONArray getTransactionHistory(TransactionParams transactionParams) {
        return ServiceController.makeCalls(TRANSACTIONS+"?page="+transactionParams.getPage()
                +"&limit="+transactionParams.getLimit(), secrets);
    }

    public JSONArray getBanks() {
        return ServiceController.makeCalls(GET_BANKS, secrets);
    }

    public JSONObject swap(SwapParams swapParams) {
        JSONObject payload = new JSONObject();
        payload.put("originNetwork", swapParams.getOriginatorNetwork().toString().toLowerCase());
        payload.put("destinationAddress", swapParams.getDestinationAddress());
        payload.put("destinationNetwork", swapParams.getDestinantionNetwork().toString().toLowerCase());
        payload.put("callbackUrl",swapParams.getCallBackUrl());
        return ServiceController.makeCalls(SWAP, secrets, payload);
    }


    public JSONObject createVirtualAccount(MintParams mintParams) {
        JSONObject payload = new JSONObject();
        payload.put("provider", mintParams.getProvider());
        payload.put("bank_code", mintParams.getBankCode());
        return ServiceController.makeCalls(CREATE_VIRTUAL_ACCOUNT, secrets, payload);
    }


    public JSONObject withdraw(IWithdrawParams withdrawParams) {
        JSONObject payload = new JSONObject();
        payload.put("amount", withdrawParams.getAmount());
        payload.put("address", withdrawParams.getAddress());
        payload.put("network", withdrawParams.getNetwork().toString().toLowerCase());
        payload.put("shouldSaveAddress", withdrawParams.isShouldSaveAddress());
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
        CNGNManger cngnManger = new CNGNManger("apiKey", "privateKey", "encryptionKey");
        System.out.println("----------------------BALANCE-------------------------");
        System.out.println("Fetch Balance : " + cngnManger.getBalance());

        System.out.println("----------------------TRANSACTION-------------------------");
        TransactionParams transactionParams = new TransactionParams(
          1,1
        );
        System.out.println("Fetch Transaction History : " + cngnManger.getTransactionHistory(transactionParams));


        System.out.println("----------------------SWAP-------------------------");
        SwapParams swapParams = new SwapParams(
                Network.BSC,
                "0x3d8e27756d784274C3C4CfeBCdFb2C096eE3cD0b",
                Network.ETH,
                "https://your-callback-url.com");
        System.out.println("Swap : " + cngnManger.swap(swapParams));


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

        System.out.println("----------------------WITHDRAW-------------------------");
        IWithdrawParams withdrawParams = new IWithdrawParams(
                "1000",
                "0x789...",
                Network.BSC,
                true
        );
        System.out.println("Withdraw : " + cngnManger.withdraw(withdrawParams));

        System.out.println("----------------------UPDATE EXTERNAL ACCOUNTS-------------------------");
        UpdateExternalAccountParams updateExternalAccountParams = new UpdateExternalAccountParams(
                "Test Bank",
                "Example account",
                "1234567890"
        );
        updateExternalAccountParams.addWalletAddress("bscAddress","0x3d8e....");
        updateExternalAccountParams.addWalletAddress("xbnAddress","0x3d8e2.....");
        //add other address

        System.out.println("Update External Accounts " + cngnManger.updateExternalAccounts(updateExternalAccountParams));

        System.out.println("----------------------GET BANKS-------------------------");
        System.out.println("Get Banks : " + cngnManger.getBanks());
    }
}



