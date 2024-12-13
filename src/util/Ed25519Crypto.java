package util;

import com.muquit.libsodiumjna.SodiumLibrary;
import com.muquit.libsodiumjna.SodiumUtils;
import com.muquit.libsodiumjna.exceptions.SodiumLibraryException;

import java.util.Base64;
import java.nio.charset.StandardCharsets;

public class Ed25519Crypto {

    private static boolean isInitialized = false;
    private static void initialize() throws SodiumLibraryException {
        if (!isInitialized) {
            SodiumLibrary.setLibraryPath("lib\\libsodium.dll");
            isInitialized = true;
        }
    }

    private static byte[] parseOpenSSHPrivateKey(String privateKey) {
        String[] lines = privateKey.split("\\r?\\n");

        if (!lines[0].contains("BEGIN OPENSSH PRIVATE KEY")) {
            throw new IllegalArgumentException("The provided key is not in OpenSSH format");
        }

        StringBuilder base64PrivateKey = new StringBuilder();
        for (int i = 1; i < lines.length - 1; i++) {
            base64PrivateKey.append(lines[i]);
        }

        byte[] privateKeyBytes = Base64.getDecoder().decode(base64PrivateKey.toString());

        int keyDataStart = -1;
        for (int i = 0; i < privateKeyBytes.length - 4; i++) {
            if (privateKeyBytes[i] == 0x00 && privateKeyBytes[i + 1] == 0x00 &&
                    privateKeyBytes[i + 2] == 0x00 && privateKeyBytes[i + 3] == 0x40) {
                keyDataStart = i + 4;
                break;
            }
        }

        if (keyDataStart == -1 || privateKeyBytes.length < keyDataStart + 64) {
            throw new IllegalArgumentException("Unable to find Ed25519 key data");
        }

        byte[] keyData = new byte[64];
        System.arraycopy(privateKeyBytes, keyDataStart, keyData, 0, 64);

        return keyData;
    }

    public static String decryptWithPrivateKey(String ed25519PrivateKey, String encryptedData) throws Exception {
        initialize();

        try {
            byte[] fullPrivateKey = parseOpenSSHPrivateKey(ed25519PrivateKey);
            byte[] curve25519PrivateKey;

            if (fullPrivateKey.length == 64) {
                curve25519PrivateKey = SodiumLibrary.cryptoSignEdSkTOcurveSk(fullPrivateKey);
            } else {
                throw new Exception("Invalid key length: Expected 64 bytes for full Ed25519 private key.");
            }

            byte[] encryptedBuffer = Base64.getDecoder().decode(encryptedData);

            int nonceLength = SodiumLibrary.cryptoBoxNonceBytes().intValue();
            int publicKeyLength = SodiumLibrary.crytoBoxPublicKeyBytes().intValue();

            byte[] nonce = new byte[nonceLength];
            System.arraycopy(encryptedBuffer, 0, nonce, 0, nonceLength);

            byte[] ephemeralPublicKey = new byte[publicKeyLength];
            System.arraycopy(encryptedBuffer, encryptedBuffer.length - publicKeyLength, ephemeralPublicKey, 0, publicKeyLength);

            byte[] ciphertext = new byte[encryptedBuffer.length - nonceLength - publicKeyLength];
            System.arraycopy(encryptedBuffer, nonceLength, ciphertext, 0, ciphertext.length);

            // Decrypt the ciphertext
            byte[] decryptedData = SodiumLibrary.cryptoBoxOpenEasy(ciphertext, nonce, ephemeralPublicKey, curve25519PrivateKey);

            if (decryptedData == null) {
                throw new Exception("Decryption failed");
            }

            return new String(decryptedData, StandardCharsets.UTF_8).trim();
        } catch (Exception e) {
            throw new Exception("Failed to decrypt with the provided Ed25519 private key: " + e.getMessage(), e);
        }
    }

}
