import controller.ServiceController;
import dao.Secrets;
import dao.params.DepositParams;
import dao.params.MintParams;
import dao.params.SwapParams;
import dao.params.WhiteListAddressParams;
import org.json.JSONArray;
import org.json.JSONObject;
import util.Network;

import static util.Constants.*;

public class CNGNManger {
<<<<<<< HEAD

    private static String ed25519PrivateKey = "-----BEGIN OPENSSH PRIVATE KEY-----\n" +
            "b3BlbnNzaC1rZXktdjEAAAAABG5vbmUAAAAEbm9uZQAAAAAAAAABAAAAMwAAAAtzc2gtZW\n" +
            "QyNTUxOQAAACB5B/qHuQCh8AseZlbgRmIfgPoy/6Wt/WSMGWVUORITQQAAAKDoHXUr6B11\n" +
            "KwAAAAtzc2gtZWQyNTUxOQAAACB5B/qHuQCh8AseZlbgRmIfgPoy/6Wt/WSMGWVUORITQQ\n" +
            "AAAEC2HqPVIi6SGnT0aFFfQV+cq34COXNaGPiHATR5uUuxZHkH+oe5AKHwCx5mVuBGYh+A\n" +
            "+jL/pa39ZIwZZVQ5EhNBAAAAF29tb3RheW90ZW1pNDdAZ21haWwuY29tAQIDBAUG\n" +
            "-----END OPENSSH PRIVATE KEY-----";

    private Secrets secrets;
=======
   
    private MerchantService merchantService;
>>>>>>> dee8eaf16a76caf962a30aecdf204390437f1f23

    public CNGNManger(String apiKey, String privateKey, String encryptionKey) {
        secrets = new Secrets(apiKey, privateKey, encryptionKey);
        return;
    }

    public JSONArray getBalance() {
<<<<<<< HEAD
        return ServiceController.makeCalls(GET_BALANCE, secrets);
    }

    public JSONArray getTransactionHistory() {
        return ServiceController.makeCalls(TRANSACTIONS, secrets);
=======
        return ServiceController.makeCalls(GET_BALANCE, merchantService);
    }

    public JSONArray getTransactionHistory() {
        return ServiceController.makeCalls(TRANSACTIONS, merchantService);
>>>>>>> dee8eaf16a76caf962a30aecdf204390437f1f23
    }

    public JSONObject swap(SwapParams swapParams) {
        JSONObject payload = new JSONObject();
<<<<<<< HEAD
        payload.put("amount", swapParams.getAmount());
        payload.put("address", swapParams.getAddress());
        payload.put("network", swapParams.getNetwork().toString().toLowerCase());
        return ServiceController.makeCalls(SWAP, secrets, payload);
=======
        payload.put("amount", amount);
        payload.put("address", address);
        payload.put("network", network.toString().toLowerCase());
        return ServiceController.makeCalls(SWAP, merchantService, payload);
>>>>>>> dee8eaf16a76caf962a30aecdf204390437f1f23
    }

    public JSONObject deposit(DepositParams depositParams) {
        JSONObject payload = new JSONObject();
<<<<<<< HEAD
        payload.put("amount", depositParams.getAmount());
        payload.put("bank", depositParams.getBank());
        payload.put("accountNumber", depositParams.getAccountNumber());
        return ServiceController.makeCalls(DEPOSIT, secrets, payload);
=======
        payload.put("amount", amount);
        payload.put("bank", bank);
        payload.put("accountNumber", accountNumber);
        return ServiceController.makeCalls(DEPOSIT, merchantService, payload);
>>>>>>> dee8eaf16a76caf962a30aecdf204390437f1f23
    }

    public JSONObject createVirtualAccount(MintParams mintParams) {
        JSONObject payload = new JSONObject();
<<<<<<< HEAD
        payload.put("provider", mintParams.getProvider());
        payload.put("bank_code", mintParams.getBankCode());
        return ServiceController.makeCalls(CREATE_VIRTUAL_ACCOUNT, secrets, payload);
=======
        payload.put("provider", provider);
        return ServiceController.makeCalls(CREATE_VIRTUAL_ACCOUNT, merchantService, payload);
>>>>>>> dee8eaf16a76caf962a30aecdf204390437f1f23
    }

    public JSONObject whiteList(WhiteListAddressParams whiteListAddressParams) {
        JSONObject payload = new JSONObject();
<<<<<<< HEAD
        payload.put("bscAddress", whiteListAddressParams.getBscAddress());
        payload.put("bankName", whiteListAddressParams.getBankName());
        payload.put("bankAccountNumber", whiteListAddressParams.getBankAccountNumber());
        return ServiceController.makeCalls(WHITELIST_ADDRESS, secrets, payload);
=======
        payload.put("bscAddress", bscAddress);
        payload.put("bankName", bankName);
        payload.put("bankAccountNumber", bankAccountNumber);
        return ServiceController.makeCalls(WHITELIST_ADDRESS, merchantService, payload);
>>>>>>> dee8eaf16a76caf962a30aecdf204390437f1f23
    }

    public JSONObject generateWalletAddress(Network network) {
        return ServiceController.createWallet(network);
    }


    public static void main(String[] args) {
        System.out.println("----------------------BEGIN TEST-------------------------");
        CNGNManger cngnManger = new CNGNManger("cngn_live_6VCaEbFUaJU4eKZlMMy740VFWDEA995kbAI53AtDiarqUoAWotv", ed25519PrivateKey, "kDf2DPsaU/k+Xf7gg3ZTPvkn6FlFAtyzNu4DBP2ssL7stvKPVUYF2TFyvva9m+M=");

        System.out.println("----------------------BALANCE-------------------------");
        System.out.println("Fetch Balance : " + cngnManger.getBalance());

        System.out.println("----------------------TRANSACTION-------------------------");
        System.out.println("Fetch Transaction History : " + cngnManger.getTransactionHistory());

        System.out.println("----------------------WHITELIST------------------------");
        WhiteListAddressParams whiteListAddressParams = new WhiteListAddressParams(
                "0x3d8e27756d784274C3C4CfeBCdFb2C096eE3cD0b",
                "Example Bank",
                "1234567890");
        System.out.println("WhiteList Address : " + cngnManger.whiteList(whiteListAddressParams));

        System.out.println("----------------------SWAP-------------------------");
        SwapParams swapParams = new SwapParams(
                1000,
                "",
                Network.ETH);
        System.out.println("Swap : " + cngnManger.swap(swapParams));

        System.out.println("----------------------DEPOSIT-------------------------");
        DepositParams depositParams = new DepositParams(
                1000,
                "Example Bank",
                "1234567890");
        System.out.println("Deposit : " + cngnManger.deposit(depositParams));


        System.out.println("----------------------CREATE VIRTUAL ACCOUNT-------------------------");
        MintParams mintParams = new MintParams(
                "korapay",
                "123"
        );
        System.out.println("Create Virtual Account : " + cngnManger.createVirtualAccount(mintParams));

    }
}

