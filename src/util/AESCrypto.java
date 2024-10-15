package util;

import dao.AESEncryptionResponse;
import org.json.JSONObject;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class AESCrypto {

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int IV_LENGTH = 16;
    private static final int KEY_LENGTH = 32;

    private static byte[] prepareKey(String key) throws NoSuchAlgorithmException {
        // Hash the key to ensure it's always the correct length
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = sha256.digest(key.getBytes());
        return keyBytes;
    }

    public static AESEncryptionResponse encrypt(String data, String key) throws Exception {
        byte[] iv = new byte[IV_LENGTH];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(iv);
        byte[] keyBuffer = prepareKey(key);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        SecretKeySpec secretKey = new SecretKeySpec(keyBuffer, "AES");
        IvParameterSpec ivParams = new IvParameterSpec(iv);

        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParams);

        byte[] encrypted = cipher.doFinal(data.getBytes("UTF-8"));

        return new AESEncryptionResponse(Base64.getEncoder().encodeToString(encrypted), Base64.getEncoder().encodeToString(iv));
    }

    public static String decrypt(AESEncryptionResponse encryptedData, String key) throws Exception {
        byte[] iv = Base64.getDecoder().decode(encryptedData.getIv());
        byte[] keyBuffer = prepareKey(key);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        SecretKeySpec secretKey = new SecretKeySpec(keyBuffer, "AES");
        IvParameterSpec ivParams = new IvParameterSpec(iv);

        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParams);

        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedData.getContent()));
        return new String(decrypted, "UTF-8");
    }

}
