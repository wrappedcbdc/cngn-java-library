package dao.params;


import util.Network;

public class IWithdrawParams {

    double amount;
    String address;
    Network network;
    boolean shouldSaveAddress;

    public IWithdrawParams(double amount, String address, Network network, boolean shouldSaveAddress) {
        this.amount = amount;
        this.address = address;
        this.network = network;
        this.shouldSaveAddress = shouldSaveAddress;
    }

    public IWithdrawParams() {

    }



    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
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
