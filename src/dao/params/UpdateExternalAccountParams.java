package dao.params;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UpdateExternalAccountParams {

    private List<Map<String, String>> walletAddresses;
    private String bankName;
    private String bankAccountName;
    private String bankAccountNumber;

    // Constructor
    public UpdateExternalAccountParams(String bankName, String bankAccountName, String bankAccountNumber) {
        this.walletAddresses = new ArrayList<>();
        this.bankName = bankName;
        this.bankAccountName = bankAccountName;
        this.bankAccountNumber = bankAccountNumber;
    }

    // Method to add a wallet address
    public void addWalletAddress(String type, String address) {
        this.walletAddresses.add(Map.of(type, address));
    }

    // Method to create JSON object
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();

        // Creating the walletAddress array dynamically
        JSONArray walletAddressArray = new JSONArray();
        for (Map<String, String> address : walletAddresses) {
            JSONObject addressObj = new JSONObject(address);
            walletAddressArray.put(addressObj);
        }

        jsonObject.put("walletAddress", walletAddressArray);

        // Creating the bankDetails object
        JSONObject bankDetails = new JSONObject();
        bankDetails.put("bankName", bankName);
        bankDetails.put("bankAccountName", bankAccountName);
        bankDetails.put("bankAccountNumber", bankAccountNumber);

        jsonObject.put("bankDetails", bankDetails);

        return jsonObject;
    }
}

