package controller;

import dao.AESEncryptionResponse;
import dao.Secrets;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import util.AESCrypto;
import util.Ed25519Crypto;
import util.Network;
import wallet.CryptoWallet;

import java.io.IOException;
import java.util.Map;

public class ServiceController {

    public static JSONArray makeCalls(String URL, Secrets secrets) {
        if (isNull(secrets)) {
            return new JSONArray().put(new JSONObject().put("exception", "Invalid merchant service details"));
        }

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .header("Authorization", "Bearer " + secrets.getApiKey())
                .url(URL)
                .build();
        try (Response response = client.newCall(request).execute()) {
            String resultStr = response.body().string();
            if (response.code() != 200) {
                try {
                    return new JSONArray().put(new JSONObject(resultStr));
                } catch (JSONException e) {
                    return new JSONArray().put(new JSONObject().put("error", resultStr));
                }
            } else {
                return parseSuccessResponseGET(resultStr, secrets.getPrivateKey());
            }
        } catch (IOException e) {
            return new JSONArray().put(new JSONObject().put("exception", "Network error: " + e.getMessage()));
        } catch (Exception e) {
            return new JSONArray().put(new JSONObject().put("exception", "Unexpected error: " + e.getMessage()));
        }
    }

    public static JSONObject makeCallObject(String URL, Secrets secrets) {
        if (isNull(secrets)) {
            return new JSONObject().put("exception", "Invalid merchant service details");
        }

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .header("Authorization", "Bearer " + secrets.getApiKey())
                .url(URL)
                .build();
        try (Response response = client.newCall(request).execute()) {
            String resultStr = response.body().string();
            if (response.code() != 200) {
                try {
                    return new JSONObject(resultStr);
                } catch (JSONException e) {
                    return new JSONObject().put("error", resultStr);
                }
            } else {
                return parseSuccessResponseGETS(resultStr, secrets.getPrivateKey());
            }
        } catch (IOException e) {
            return new JSONObject().put("exception", "Network error: " + e.getMessage());
        } catch (Exception e) {
            return new JSONObject().put("exception", "Unexpected error: " + e.getMessage());
        }
    }

    public static JSONObject makeCalls(String URL, Secrets secrets, JSONObject params) {
        if (isNull(secrets)) {
            return new JSONObject().put("exception", "Invalid merchant service details");
        }

        try {
            AESEncryptionResponse aesEncryptionResponse = AESCrypto.encrypt(params.toString(), secrets.getEncryptionKey());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("content", aesEncryptionResponse.getContent());
            jsonObject.put("iv", aesEncryptionResponse.getIv());

            OkHttpClient client = new OkHttpClient();
            RequestBody body = RequestBody.create(jsonObject.toString(), MediaType.parse("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url(URL)
                    .header("Authorization", "Bearer " + secrets.getApiKey())
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                String resultStr = response.body().string();
                if (response.code() != 200) {
                    return new JSONObject(resultStr);
                } else {
                    return parseSuccessResponsePOST(resultStr, secrets.getPrivateKey());
                }
            }
        } catch (IOException e) {
            return new JSONObject().put("exception", "Network error: " + e.getMessage());
        } catch (JSONException e) {
            return new JSONObject().put("exception", "JSON error: " + e.getMessage());
        } catch (Exception e) {
            return new JSONObject().put("exception", "Unexpected error: " + e.getMessage());
        }
    }

    public static JSONObject makeCalls(String URL, Secrets secrets, Map<String, Object> params) {
        JSONObject paramsJson = new JSONObject(params);
        if (isNull(secrets)) {
            return new JSONObject().put("exception", "Invalid merchant service details");
        }

        try {
            AESEncryptionResponse aesEncryptionResponse = AESCrypto.encrypt(paramsJson.toString(4), secrets.getEncryptionKey());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("content", aesEncryptionResponse.getContent());
            jsonObject.put("iv", aesEncryptionResponse.getIv());

            OkHttpClient client = new OkHttpClient();
            RequestBody body = RequestBody.create(jsonObject.toString(), MediaType.parse("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url(URL)
                    .header("Authorization", "Bearer " + secrets.getApiKey())
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                String resultStr = response.body().string();
                if (response.code() != 200) {
                    return new JSONObject(resultStr);
                } else {
                    return parseSuccessResponsePOST(resultStr, secrets.getPrivateKey());
                }
            }
        } catch (IOException e) {
            return new JSONObject().put("exception", "Network error: " + e.getMessage());
        } catch (JSONException e) {
            return new JSONObject().put("exception", "JSON error: " + e.getMessage());
        } catch (Exception e) {
            return new JSONObject().put("exception", "Unexpected error: " + e.getMessage());
        }
    }

    public static JSONObject createWallet(Network network) {
        return new JSONObject(new CryptoWallet(network).getWalletDetails());
    }

    private static JSONArray parseSuccessResponseGET(String resultStr, String privateKey) throws Exception {
        JSONObject responseData = new JSONObject(resultStr);
        String encryptedData = responseData.getString("data");
        return new JSONArray(Ed25519Crypto.decryptWithPrivateKey(privateKey, encryptedData));
    }

    private static JSONObject parseSuccessResponseGETS(String resultStr, String privateKey) throws Exception {
        JSONObject responseData = new JSONObject(resultStr);
        String encryptedData = responseData.getString("data");
        return new JSONObject(Ed25519Crypto.decryptWithPrivateKey(privateKey, encryptedData));
    }

    private static JSONObject parseSuccessResponsePOST(String resultStr, String privateKey) throws Exception {
        JSONObject responseData = new JSONObject(resultStr);
        String encryptedData = responseData.getString("data");
        return new JSONObject(Ed25519Crypto.decryptWithPrivateKey(privateKey, encryptedData));
    }

    private static boolean isNull(Secrets secrets) {
        return secrets.getPrivateKey() == null || secrets.getPrivateKey().isBlank()
                || secrets.getApiKey() == null || secrets.getApiKey().isBlank()
                || secrets.getEncryptionKey() == null || secrets.getEncryptionKey().isBlank();
    }
}
