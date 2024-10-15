package dao;

import jnr.constants.Constant;
import org.json.JSONObject;
import util.Constants;

public class MerchantService {
    private String apiKey;
    private String privateKey;
    private String encryptionKey;

    public MerchantService(String apiKey, String privateKey, String encryptionKey) {
        this.apiKey = apiKey;
        this.privateKey = privateKey;
        this.encryptionKey = encryptionKey;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getEncryptionKey() {
        return encryptionKey;
    }

    public void setEncryptionKey(String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }
}
