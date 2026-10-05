package co.cngn.sdk;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bouncycastle.math.ec.rfc8032.Ed25519;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class CngnCryptoTest {
    private static final String NACL_BOX_VECTOR = "AwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDnjFK7eDgrvJLL/GOccUK+glTymjxdmp8CfFpye8Bh2bj/F514xMD1oSKdc+HWqsqIr3RfOLUlJYDPZ3zt9DOjTrRzLYz7HtwwXgUpcduzQKWhQUNNEdFugWHDlh9WQ==";

    static String pem(byte[] seed) throws Exception {
        byte[] publicKey = new byte[32];
        Ed25519.generatePublicKey(seed, 0, publicKey, 0);
        ByteArrayOutputStream publicBlob = new ByteArrayOutputStream();
        DataOutputStream pub = new DataOutputStream(publicBlob);
        field(pub, "ssh-ed25519".getBytes(StandardCharsets.US_ASCII));
        field(pub, publicKey);
        ByteArrayOutputStream privateBlob = new ByteArrayOutputStream();
        DataOutputStream priv = new DataOutputStream(privateBlob);
        priv.writeInt(42); priv.writeInt(42);
        field(priv, "ssh-ed25519".getBytes(StandardCharsets.US_ASCII));
        field(priv, publicKey);
        byte[] secret = new byte[64];
        System.arraycopy(seed, 0, secret, 0, 32);
        System.arraycopy(publicKey, 0, secret, 32, 32);
        field(priv, secret);
        field(priv, new byte[0]);
        for (int pad = 1; privateBlob.size() % 8 != 0; pad++) priv.writeByte(pad);
        ByteArrayOutputStream outerBlob = new ByteArrayOutputStream();
        DataOutputStream outer = new DataOutputStream(outerBlob);
        outer.write("openssh-key-v1\0".getBytes(StandardCharsets.US_ASCII));
        field(outer, "none".getBytes(StandardCharsets.US_ASCII));
        field(outer, "none".getBytes(StandardCharsets.US_ASCII));
        field(outer, new byte[0]);
        outer.writeInt(1);
        field(outer, publicBlob.toByteArray());
        field(outer, privateBlob.toByteArray());
        return "-----BEGIN OPENSSH PRIVATE KEY-----\n"
                + Base64.getMimeEncoder(70, new byte[]{'\n'}).encodeToString(outerBlob.toByteArray())
                + "\n-----END OPENSSH PRIVATE KEY-----";
    }

    private static void field(DataOutputStream out, byte[] bytes) throws Exception {
        out.writeInt(bytes.length); out.write(bytes);
    }

    @Test void decryptsNaClBoxKnownAnswer() throws Exception {
        byte[] seed = new byte[32]; Arrays.fill(seed, (byte) 1);
        assertArrayEquals(seed, CngnCrypto.parseOpenSshSeed(pem(seed)));
        var result = CngnCrypto.decryptResponse(NACL_BOX_VECTOR, seed, new ObjectMapper());
        assertEquals("150000.00", result.get("balance").textValue());
        assertEquals("CNGN", result.get("asset_code").textValue());
    }

    @Test void rejectsTamperedBox() {
        byte[] seed = new byte[32]; Arrays.fill(seed, (byte) 1);
        byte[] tampered = Base64.getDecoder().decode(NACL_BOX_VECTOR);
        tampered[40] ^= 1;
        assertEquals(CngnException.Kind.DECRYPTION, assertThrows(CngnException.class,
                () -> CngnCrypto.decryptResponse(Base64.getEncoder().encodeToString(tampered),
                        seed, new ObjectMapper())).kind());
    }

    @Test void aesUsesCbcEnvelopeAndSha256Key() {
        byte[] key = CngnCrypto.aesKey("dashboard-secret");
        byte[] plain = "{\"amount\":100}".getBytes(StandardCharsets.UTF_8);
        Map<String, String> one = CngnCrypto.encrypt(plain, key);
        Map<String, String> two = CngnCrypto.encrypt(plain, key);
        assertEquals(16, Base64.getDecoder().decode(one.get("iv")).length);
        assertNotEquals(one.get("iv"), two.get("iv"));
        assertArrayEquals(plain, CngnCrypto.decryptAes(one, key));
        assertArrayEquals(plain, CngnCrypto.decryptAes(two, key));
    }
}
