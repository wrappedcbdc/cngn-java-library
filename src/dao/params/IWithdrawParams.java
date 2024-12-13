package dao.params;


import util.Network;

public class IWithdrawParams {

    String amount;
    String address;
    Network network;
    boolean shouldSaveAddress;

    public IWithdrawParams(String amount, String address, Network network, boolean shouldSaveAddress) {
        this.amount = amount;
        this.address = address;
        this.network = network;
        this.shouldSaveAddress = shouldSaveAddress;
    }

    public IWithdrawParams() {

    }



    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
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

    public boolean isShouldSaveAddress() {
        return shouldSaveAddress;
    }

    public void setShouldSaveAddress(boolean shouldSaveAddress) {
        this.shouldSaveAddress = shouldSaveAddress;
    }
}
