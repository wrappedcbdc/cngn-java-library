package dao.params;

import util.Network;

public class SwapParams {
    private int amount;
    private String address;
    private Network network;

    public SwapParams(int amount, String address, Network network) {
        this.amount = amount;
        this.address = address;
        this.network = network;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Network getNetwork() {
        return network;
    }

    public void setNetwork(Network network) {
        this.network = network;
    }
}
