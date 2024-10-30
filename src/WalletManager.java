import controller.ServiceController;
import org.json.JSONObject;
import util.Network;
import wallet.CryptoWallet;

public class WalletManager{

    /*
    This class allows you to generate wallet only
     */
    public static JSONObject generateWalletAddress(Network network) {
        return ServiceController.createWallet(network);
    }

    public static void main(String[] args) {
        System.out.println("----------------------BEGIN TEST-------------------------");
        System.out.println("WALLET DETAILS : "+ WalletManager.generateWalletAddress(Network.ETH));
    }
}
