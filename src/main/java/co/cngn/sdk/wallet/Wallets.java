package co.cngn.sdk.wallet;

import org.bouncycastle.math.ec.rfc8032.Ed25519;
import org.web3j.crypto.Bip32ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.crypto.MnemonicUtils;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HexFormat;

/** Local BIP-39/44 wallet utilities. Never send generated secrets to cNGN or log them. */
public final class Wallets {
    public enum Network { ETHEREUM, BASE, BSC, POLYGON, TRON, SOLANA, STELLAR, XBN }

    public record GeneratedWallet(Network network, String address, String publicKey,
                                  String privateKey, String mnemonic) {
        @Override public String toString() {
            return "GeneratedWallet[network=" + network + ", address=" + address + ", secrets=redacted]";
        }
    }

    private static final String BASE58 = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
    private static final String BASE32 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final int HARDENED = Bip32ECKeyPair.HARDENED_BIT;
    private static final SecureRandom RANDOM = new SecureRandom();

    private Wallets() {}

    public static GeneratedWallet generate(Network network) {
        byte[] entropy = new byte[16];
        RANDOM.nextBytes(entropy);
        try {
            return fromMnemonic(network, MnemonicUtils.generateMnemonic(entropy));
        } finally {
            Arrays.fill(entropy, (byte) 0);
        }
    }

    public static GeneratedWallet fromMnemonic(Network network, String phrase) {
        if (network == null) throw new IllegalArgumentException("network is required");
        if (phrase == null || !MnemonicUtils.validateMnemonic(phrase)) {
            throw new IllegalArgumentException("Invalid BIP-39 mnemonic");
        }
        byte[] seed = MnemonicUtils.generateSeed(phrase, "");
        try {
            if (network == Network.SOLANA || network == Network.STELLAR || network == Network.XBN) {
                return ed25519(network, seed, phrase);
            }
            int coin = network == Network.TRON ? 195 : 60;
            Bip32ECKeyPair root = Bip32ECKeyPair.generateKeyPair(seed);
            Bip32ECKeyPair key = Bip32ECKeyPair.deriveKeyPair(root,
                    new int[]{44 | HARDENED, coin | HARDENED, 0 | HARDENED, 0, 0});
            byte[] privateBytes = fixed32(key.getPrivateKey());
            try {
                String publicKey = HexFormat.of().formatHex(key.getPublicKeyPoint().getEncoded(false));
                String evmAddress = Keys.toChecksumAddress("0x" + Keys.getAddress(key));
                String address = network == Network.TRON ? tronAddress(evmAddress) : evmAddress;
                return new GeneratedWallet(network, address, publicKey,
                        HexFormat.of().formatHex(privateBytes), phrase);
            } finally {
                Arrays.fill(privateBytes, (byte) 0);
            }
        } finally {
            Arrays.fill(seed, (byte) 0);
        }
    }

    private static GeneratedWallet ed25519(Network network, byte[] seed, String phrase) {
        byte[] node = hmac("ed25519 seed".getBytes(StandardCharsets.US_ASCII), seed);
        try {
            int[] path = switch (network) {
                case SOLANA -> new int[]{44, 501, 0, 0};
                case STELLAR -> new int[]{44, 148, 0};
                case XBN -> new int[]{44, 703, 0};
                default -> throw new IllegalArgumentException("Unsupported Ed25519 network");
            };
            for (int index : path) {
                byte[] input = ByteBuffer.allocate(37).put((byte) 0)
                        .put(node, 0, 32).putInt(index | HARDENED).array();
                byte[] chain = Arrays.copyOfRange(node, 32, 64);
                byte[] next;
                try {
                    next = hmac(chain, input);
                } finally {
                    Arrays.fill(chain, (byte) 0);
                    Arrays.fill(input, (byte) 0);
                }
                Arrays.fill(node, (byte) 0);
                node = next;
            }
            byte[] privateKey = Arrays.copyOf(node, 32);
            try {
                byte[] publicKey = new byte[32];
                Ed25519.generatePublicKey(privateKey, 0, publicKey, 0);
                if (network == Network.SOLANA) {
                    byte[] secret = new byte[64];
                    System.arraycopy(privateKey, 0, secret, 0, 32);
                    System.arraycopy(publicKey, 0, secret, 32, 32);
                    try {
                        String address = encodeBase58(publicKey);
                        return new GeneratedWallet(network, address, address, encodeBase58(secret), phrase);
                    } finally {
                        Arrays.fill(secret, (byte) 0);
                    }
                }
                String address = strKey((byte) 0x30, publicKey);
                return new GeneratedWallet(network, address, address, strKey((byte) 0x90, privateKey), phrase);
            } finally {
                Arrays.fill(privateKey, (byte) 0);
            }
        } finally {
            Arrays.fill(node, (byte) 0);
        }
    }

    public static boolean validateAddress(Network network, String address) {
        if (network == null || address == null) return false;
        try {
            return switch (network) {
                case ETHEREUM, BASE, BSC, POLYGON -> validEvm(address);
                case TRON -> validTron(address);
                case SOLANA -> decodeBase58(address).length == 32;
                case STELLAR, XBN -> validStellar(address);
            };
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static boolean validEvm(String address) {
        if (address.length() != 42 || !address.startsWith("0x")) return false;
        String digits = address.substring(2);
        if (!digits.matches("[0-9a-fA-F]{40}")) return false;
        if (digits.equals(digits.toLowerCase(java.util.Locale.ROOT))
                || digits.equals(digits.toUpperCase(java.util.Locale.ROOT))) return true;
        return address.equals(Keys.toChecksumAddress("0x" + digits.toLowerCase(java.util.Locale.ROOT)));
    }

    private static byte[] fixed32(BigInteger value) {
        byte[] encoded = value.toByteArray();
        byte[] fixed = new byte[32];
        System.arraycopy(encoded, Math.max(0, encoded.length - 32), fixed,
                Math.max(0, 32 - encoded.length), Math.min(32, encoded.length));
        return fixed;
    }

    private static String tronAddress(String evmAddress) {
        byte[] payload = new byte[21];
        payload[0] = 0x41;
        byte[] raw = HexFormat.of().parseHex(evmAddress.substring(2));
        System.arraycopy(raw, 0, payload, 1, 20);
        byte[] checksum = sha256(sha256(payload));
        byte[] address = Arrays.copyOf(payload, 25);
        System.arraycopy(checksum, 0, address, 21, 4);
        return encodeBase58(address);
    }

    private static boolean validTron(String address) {
        byte[] bytes = decodeBase58(address);
        if (bytes.length != 25 || bytes[0] != 0x41) return false;
        byte[] checksum = sha256(sha256(Arrays.copyOf(bytes, 21)));
        return MessageDigest.isEqual(Arrays.copyOfRange(bytes, 21, 25), Arrays.copyOf(checksum, 4));
    }

    private static String encodeBase58(byte[] bytes) {
        BigInteger n = new BigInteger(1, bytes);
        StringBuilder out = new StringBuilder();
        while (n.signum() > 0) {
            BigInteger[] qr = n.divideAndRemainder(BigInteger.valueOf(58));
            out.append(BASE58.charAt(qr[1].intValue()));
            n = qr[0];
        }
        for (byte b : bytes) { if (b == 0) out.append('1'); else break; }
        return out.reverse().toString();
    }

    private static byte[] decodeBase58(String encoded) {
        BigInteger n = BigInteger.ZERO;
        for (int i = 0; i < encoded.length(); i++) {
            int digit = BASE58.indexOf(encoded.charAt(i));
            if (digit < 0) throw new IllegalArgumentException("Invalid Base58 digit");
            n = n.multiply(BigInteger.valueOf(58)).add(BigInteger.valueOf(digit));
        }
        byte[] bytes = n.equals(BigInteger.ZERO) ? new byte[0] : n.toByteArray();
        if (bytes.length > 0 && bytes[0] == 0) bytes = Arrays.copyOfRange(bytes, 1, bytes.length);
        int zeros = 0;
        while (zeros < encoded.length() && encoded.charAt(zeros) == '1') zeros++;
        byte[] result = new byte[zeros + bytes.length];
        System.arraycopy(bytes, 0, result, zeros, bytes.length);
        return result;
    }

    private static String strKey(byte version, byte[] key) {
        byte[] payload = new byte[35];
        payload[0] = version;
        System.arraycopy(key, 0, payload, 1, 32);
        int crc = crc16(payload, 33);
        payload[33] = (byte) crc;
        payload[34] = (byte) (crc >>> 8);
        StringBuilder out = new StringBuilder(56);
        int bits = 0, value = 0;
        for (byte b : payload) {
            value = (value << 8) | (b & 255);
            bits += 8;
            while (bits >= 5) {
                bits -= 5;
                out.append(BASE32.charAt((value >>> bits) & 31));
            }
        }
        if (bits != 0) out.append(BASE32.charAt((value << (5 - bits)) & 31));
        return out.toString();
    }

    private static boolean validStellar(String address) {
        if (address.length() != 56) return false;
        byte[] decoded = new byte[35];
        int bits = 0, value = 0, index = 0;
        for (int i = 0; i < address.length(); i++) {
            int digit = BASE32.indexOf(address.charAt(i));
            if (digit < 0) return false;
            value = (value << 5) | digit;
            bits += 5;
            if (bits >= 8) {
                bits -= 8;
                if (index >= decoded.length) return false;
                decoded[index++] = (byte) (value >>> bits);
            }
        }
        int crc = crc16(decoded, 33);
        return index == 35 && decoded[0] == 0x30
                && decoded[33] == (byte) crc && decoded[34] == (byte) (crc >>> 8);
    }

    private static int crc16(byte[] bytes, int length) {
        int crc = 0;
        for (int i = 0; i < length; i++) {
            crc ^= (bytes[i] & 255) << 8;
            for (int bit = 0; bit < 8; bit++) {
                crc = (crc & 0x8000) != 0 ? ((crc << 1) ^ 0x1021) & 0xffff : (crc << 1) & 0xffff;
            }
        }
        return crc;
    }

    private static byte[] sha256(byte[] data) {
        try { return MessageDigest.getInstance("SHA-256").digest(data); }
        catch (Exception e) { throw new IllegalStateException(e); }
    }

    private static byte[] hmac(byte[] key, byte[] data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(key, "HmacSHA512"));
            return mac.doFinal(data);
        } catch (Exception e) { throw new IllegalStateException(e); }
    }
}
