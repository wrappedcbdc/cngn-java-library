package wallet;

import com.google.gson.Gson;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicHierarchy;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Hash;
import org.web3j.crypto.MnemonicUtils;
import org.web3j.utils.Numeric;
import util.Network;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.*;

public class CryptoWallet {



    private static final int MNEMONIC_ENTROPY_BYTES = 128;
    private static final Map<Network, String> DERIVATION_PATHS = new HashMap<>();

    private String mnemonic;
    private String privateKey;
    private String address;
    private Network network;

    static {
        DERIVATION_PATHS.put(Network.ETH, "m/44'/60'/0'/0/0");
        DERIVATION_PATHS.put(Network.BSC, "m/44'/60'/0'/0/0");
        DERIVATION_PATHS.put(Network.ATC, "m/44'/60'/0'/0/0");
        DERIVATION_PATHS.put(Network.MATIC, "m/44'/60'/0'/0/0");
        DERIVATION_PATHS.put(Network.TRX, "m/44'/195'/0'/0/0");
        DERIVATION_PATHS.put(Network.XBN, "m/44'/703'/0'");
    }

    public CryptoWallet(Network network) {
        this.network = network;
        this.mnemonic = generateMnemonic();
        if (network == Network.XBN) {
            initializeXbnWallet();
        } else {
            initializeWallet();
        }
    }

    private String generateMnemonic() {
        byte[] randomBytes = new byte[MNEMONIC_ENTROPY_BYTES / 8];
        new SecureRandom().nextBytes(randomBytes);
        return MnemonicUtils.generateMnemonic(randomBytes);
    }

    private void initializeWallet() {
        this.privateKey = getPrivateKeyFromMnemonic(this.mnemonic, this.network);
        String publicKey = getPublicKey(this.privateKey);
        this.address = getAddressFromPublicKey(publicKey, this.network);
    }

    private void initializeXbnWallet() {
        byte[] seed = MnemonicUtils.generateSeed(this.mnemonic, "");
        ECKeyPair ecKeyPair = deriveKeyPairFromSeed(seed, DERIVATION_PATHS.get(Network.XBN));
        this.privateKey = Numeric.toHexStringWithPrefix(ecKeyPair.getPrivateKey());
        this.address = getXbnAddressFromPublicKey(ecKeyPair.getPublicKey().toString(16));
    }

    private String getPublicKey(String privateKey) {
        ECKeyPair ecKeyPair = ECKeyPair.create(Numeric.toBigInt(privateKey));
        return Numeric.toHexString(ecKeyPair.getPublicKey().toByteArray());
    }

    private String getPrivateKeyFromMnemonic(String mnemonic, Network network) {
        byte[] seed = MnemonicUtils.generateSeed(mnemonic, "");
        ECKeyPair ecKeyPair = deriveKeyPairFromSeed(seed, DERIVATION_PATHS.get(network));
        return Numeric.toHexStringWithPrefix(ecKeyPair.getPrivateKey());
    }

    private String getAddressFromPublicKey(String publicKey, Network network) {
        switch (network) {
            case ETH:
            case BSC:
            case ATC:
            case MATIC:
                return getEthereumStyleAddress(publicKey);
            case TRX:
                return convertToTronAddress(getEthereumStyleAddress(publicKey));
            default:
                throw new IllegalArgumentException("Unsupported network: " + network);
        }
    }

    private static String getEthereumStyleAddress(String publicKey) {
        String cleanPublicKey = publicKey.startsWith("04") ? publicKey.substring(2) : publicKey;
        byte[] hash = Hash.sha3(Numeric.hexStringToByteArray(cleanPublicKey));
        String hexHash = Numeric.toHexString(hash);
        if (hexHash.length() < 40) {
            throw new IllegalArgumentException("Invalid public key or hash length for Ethereum address generation.");
        }
        return "0x" + hexHash.substring(hexHash.length() - 40);
    }

    private String convertToTronAddress(String ethAddress) {
        ethAddress = Numeric.cleanHexPrefix(ethAddress);
        byte[] ethAddressBytes = Numeric.hexStringToByteArray(ethAddress);
        byte[] tronAddressBytes = new byte[ethAddressBytes.length + 1];
        tronAddressBytes[0] = 0x41;
        System.arraycopy(ethAddressBytes, 0, tronAddressBytes, 1, ethAddressBytes.length);
        return Numeric.toHexString(tronAddressBytes);
    }

    private String getXbnAddressFromPublicKey(String publicKey) {
        byte[] hash = Hash.sha3(Numeric.hexStringToByteArray(publicKey));
        return "XBN" + Numeric.toHexString(Arrays.copyOfRange(hash, 12, hash.length));
    }

    private ECKeyPair deriveKeyPairFromSeed(byte[] seed, String derivationPath) {
        DeterministicKey rootPrivateKey = HDKeyDerivation.createMasterPrivateKey(seed);
        DeterministicHierarchy hierarchy = new DeterministicHierarchy(rootPrivateKey);
        List<ChildNumber> path = parseDerivationPath(derivationPath);
        DeterministicKey derivedKey = hierarchy.get(path, true, true);
        BigInteger privateKey = derivedKey.getPrivKey();
        BigInteger publicKey = derivedKey.getPubKeyPoint().getXCoord().toBigInteger();
        return new ECKeyPair(privateKey, publicKey);
    }

    private List<ChildNumber> parseDerivationPath(String derivationPath) {
        String[] pathArray = derivationPath.split("/");
        List<ChildNumber> path = new ArrayList<>();
        for (String part : pathArray) {
            if (part.isEmpty() || part.equals("m")) continue;
            boolean hardened = part.endsWith("'");
            int index = Integer.parseInt(hardened ? part.substring(0, part.length() - 1) : part);
            path.add(new ChildNumber(index, hardened));
        }
        return path;
    }

    public String getWalletDetails() {
        Map<String, Object> walletDetails = new HashMap<>();
        walletDetails.put("mnemonic", Arrays.asList(this.mnemonic.split(" ")));
        walletDetails.put("privateKey", this.privateKey);
        walletDetails.put("address", this.address);
        walletDetails.put("network", this.network.name());
        return new Gson().toJson(walletDetails);
    }

}
