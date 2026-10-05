package co.cngn.sdk;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bouncycastle.crypto.engines.Salsa20Engine;
import org.bouncycastle.crypto.engines.XSalsa20Engine;
import org.bouncycastle.crypto.macs.Poly1305;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.math.ec.rfc7748.X25519;
import org.bouncycastle.math.ec.rfc8032.Ed25519;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;

/** cNGN wire cryptography: AES-256-CBC requests and authenticated NaCl Box responses. */
final class CngnCrypto {
    private static final byte[] SSH_MAGIC = "openssh-key-v1\0".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] ED25519_TYPE = "ssh-ed25519".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] SALSA_SIGMA = "expand 32-byte k".getBytes(StandardCharsets.US_ASCII);
    private static final SecureRandom RANDOM = new SecureRandom();

    private CngnCrypto() {}

    static byte[] aesKey(String encryptionKey) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(
                    encryptionKey.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new CngnException(CngnException.Kind.ENCRYPTION, "SHA-256 is unavailable", e);
        }
    }

    static Map<String, String> encrypt(byte[] plain, byte[] key) {
        if (key.length != 32) throw new CngnException(CngnException.Kind.ENCRYPTION,
                "AES-256 requires a 32-byte key");
        byte[] iv = new byte[16];
        RANDOM.nextBytes(iv);
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
            return Map.of("content", Base64.getEncoder().encodeToString(cipher.doFinal(plain)),
                    "iv", Base64.getEncoder().encodeToString(iv));
        } catch (Exception e) {
            throw new CngnException(CngnException.Kind.ENCRYPTION, "Request encryption failed", e);
        }
    }

    static byte[] decryptAes(Map<String, String> envelope, byte[] key) {
        try {
            byte[] iv = Base64.getDecoder().decode(envelope.get("iv"));
            if (iv.length != 16 || key.length != 32) throw new IllegalArgumentException("Invalid key or IV length");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
            return cipher.doFinal(Base64.getDecoder().decode(envelope.get("content")));
        } catch (Exception e) {
            throw new CngnException(CngnException.Kind.DECRYPTION, "AES decryption failed", e);
        }
    }

    static byte[] parseOpenSshSeed(String pem) {
        String header = "-----BEGIN OPENSSH PRIVATE KEY-----";
        String footer = "-----END OPENSSH PRIVATE KEY-----";
        String trimmed = pem.trim();
        if (!trimmed.startsWith(header) || !trimmed.endsWith(footer)) {
            throw new CngnException(CngnException.Kind.CONFIGURATION,
                    "Expected an unencrypted OpenSSH Ed25519 private key");
        }
        byte[] blob = null;
        byte[] privateBlock = null;
        byte[] secret = null;
        byte[] seed = null;
        byte[] derivedPublic = null;
        try {
            String body = trimmed.substring(header.length(), trimmed.length() - footer.length())
                    .replaceAll("\\s+", "");
            blob = Base64.getDecoder().decode(body);
            ByteBuffer outer = ByteBuffer.wrap(blob).order(ByteOrder.BIG_ENDIAN);
            if (outer.remaining() < SSH_MAGIC.length) throw new IllegalArgumentException("Truncated key");
            byte[] magic = new byte[SSH_MAGIC.length];
            outer.get(magic);
            if (!Arrays.equals(magic, SSH_MAGIC)) throw new IllegalArgumentException("Invalid OpenSSH magic");
            if (!Arrays.equals(readString(outer), "none".getBytes(StandardCharsets.US_ASCII))) {
                throw new IllegalArgumentException("Encrypted OpenSSH keys are unsupported");
            }
            if (!Arrays.equals(readString(outer), "none".getBytes(StandardCharsets.US_ASCII))
                    || readString(outer).length != 0 || outer.getInt() != 1) {
                throw new IllegalArgumentException("Unsupported OpenSSH key format");
            }
            byte[] publicBlob = readString(outer);
            ByteBuffer pub = ByteBuffer.wrap(publicBlob).order(ByteOrder.BIG_ENDIAN);
            if (!Arrays.equals(readString(pub), ED25519_TYPE)) {
                throw new IllegalArgumentException("Expected an Ed25519 key");
            }
            byte[] publicKey = readString(pub);
            if (publicKey.length != 32 || pub.hasRemaining()) throw new IllegalArgumentException("Invalid public key");
            privateBlock = readString(outer);
            ByteBuffer inner = ByteBuffer.wrap(privateBlock).order(ByteOrder.BIG_ENDIAN);
            if (outer.hasRemaining() || inner.getInt() != inner.getInt()
                    || !Arrays.equals(readString(inner), ED25519_TYPE)) {
                throw new IllegalArgumentException("Invalid OpenSSH private block");
            }
            byte[] innerPublic = readString(inner);
            secret = readString(inner);
            if (secret.length != 64 || !Arrays.equals(publicKey, innerPublic)
                    || !MessageDigest.isEqual(Arrays.copyOfRange(secret, 32, 64), publicKey)) {
                throw new IllegalArgumentException("OpenSSH public and private keys disagree");
            }
            seed = Arrays.copyOf(secret, 32);
            derivedPublic = new byte[32];
            Ed25519.generatePublicKey(seed, 0, derivedPublic, 0);
            if (!MessageDigest.isEqual(derivedPublic, publicKey)) {
                throw new IllegalArgumentException("OpenSSH seed does not match public key");
            }
            readString(inner); // comment
            int padding = 1;
            while (inner.hasRemaining()) {
                if (inner.get() != (byte) padding++) throw new IllegalArgumentException("Invalid OpenSSH padding");
            }
            return seed;
        } catch (IllegalArgumentException | java.nio.BufferUnderflowException e) {
            if (seed != null) Arrays.fill(seed, (byte) 0);
            throw new CngnException(CngnException.Kind.CONFIGURATION, "Invalid OpenSSH Ed25519 private key: "
                    + e.getMessage(), e);
        } finally {
            if (blob != null) Arrays.fill(blob, (byte) 0);
            if (privateBlock != null) Arrays.fill(privateBlock, (byte) 0);
            if (secret != null) Arrays.fill(secret, (byte) 0);
            if (derivedPublic != null) Arrays.fill(derivedPublic, (byte) 0);
        }
    }

    private static byte[] readString(ByteBuffer buffer) {
        if (buffer.remaining() < 4) throw new IllegalArgumentException("Truncated OpenSSH field");
        int length = buffer.getInt();
        if (length < 0 || length > buffer.remaining()) throw new IllegalArgumentException("Truncated OpenSSH field");
        byte[] value = new byte[length];
        buffer.get(value);
        return value;
    }

    static JsonNode decryptResponse(String encoded, byte[] edSeed, ObjectMapper mapper) {
        byte[] hash = null;
        byte[] curveSecret = null;
        byte[] shared = null;
        byte[] boxKey = null;
        byte[] polyKey = null;
        byte[] plain = null;
        try {
            byte[] blob = Base64.getDecoder().decode(encoded);
            if (blob.length < 72) throw new IllegalArgumentException("Response box is too short");
            byte[] nonce = Arrays.copyOfRange(blob, 0, 24);
            byte[] mac = Arrays.copyOfRange(blob, 24, 40);
            byte[] ciphertext = Arrays.copyOfRange(blob, 40, blob.length - 32);
            byte[] ephemeralPublic = Arrays.copyOfRange(blob, blob.length - 32, blob.length);
            hash = MessageDigest.getInstance("SHA-512").digest(edSeed);
            curveSecret = Arrays.copyOf(hash, 32);
            curveSecret[0] &= (byte) 248;
            curveSecret[31] &= (byte) 127;
            curveSecret[31] |= (byte) 64;
            shared = new byte[32];
            if (!X25519.calculateAgreement(curveSecret, 0, ephemeralPublic, 0, shared, 0)) {
                throw new IllegalArgumentException("Invalid ephemeral public key");
            }
            boxKey = hsalsa20(shared);
            XSalsa20Engine stream = new XSalsa20Engine();
            stream.init(false, new ParametersWithIV(new KeyParameter(boxKey), nonce));
            polyKey = new byte[32];
            stream.processBytes(new byte[32], 0, 32, polyKey, 0);
            Poly1305 poly = new Poly1305();
            poly.init(new KeyParameter(polyKey));
            poly.update(ciphertext, 0, ciphertext.length);
            byte[] actualMac = new byte[16];
            poly.doFinal(actualMac, 0);
            if (!MessageDigest.isEqual(mac, actualMac)) throw new IllegalArgumentException("Invalid response MAC");
            plain = new byte[ciphertext.length];
            stream.processBytes(ciphertext, 0, ciphertext.length, plain, 0);
            JsonNode json = mapper.readTree(plain);
            if (json == null || !json.isContainerNode()) throw new IllegalArgumentException("Expected JSON object or array");
            return json;
        } catch (Exception e) {
            throw new CngnException(CngnException.Kind.DECRYPTION,
                    "Failed to authenticate or decrypt the cNGN response", e);
        } finally {
            if (hash != null) Arrays.fill(hash, (byte) 0);
            if (curveSecret != null) Arrays.fill(curveSecret, (byte) 0);
            if (shared != null) Arrays.fill(shared, (byte) 0);
            if (boxKey != null) Arrays.fill(boxKey, (byte) 0);
            if (polyKey != null) Arrays.fill(polyKey, (byte) 0);
            if (plain != null) Arrays.fill(plain, (byte) 0);
        }
    }

    private static byte[] hsalsa20(byte[] key) {
        int[] state = new int[16];
        ByteBuffer sigma = ByteBuffer.wrap(SALSA_SIGMA).order(ByteOrder.LITTLE_ENDIAN);
        ByteBuffer words = ByteBuffer.wrap(key).order(ByteOrder.LITTLE_ENDIAN);
        state[0] = sigma.getInt(); state[5] = sigma.getInt();
        state[10] = sigma.getInt(); state[15] = sigma.getInt();
        for (int i = 1; i <= 4; i++) state[i] = words.getInt();
        for (int i = 11; i <= 14; i++) state[i] = words.getInt();
        int[] output = new int[16];
        Salsa20Engine.salsaCore(20, state, output);
        ByteBuffer result = ByteBuffer.allocate(32).order(ByteOrder.LITTLE_ENDIAN);
        for (int index : new int[]{0, 5, 10, 15, 6, 7, 8, 9}) result.putInt(output[index] - state[index]);
        return result.array();
    }
}
