package dao.params;

import util.Network;

public class SwapQuoteParams {
    private double amount;
    private String destinationAddress;
    private Network originNetwork;
    private Network destinationNetwork;

    public SwapQuoteParams(double amount, String destinationAddress, Network originNetwork, Network destinationNetwork) {
        this.amount = amount;
        this.destinationAddress = destinationAddress;
        this.originNetwork = originNetwork;
        this.destinationNetwork = destinationNetwork;
    }

    public SwapQuoteParams() {
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(String destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public Network getOriginNetwork() {
        return originNetwork;
    }

    public void setOriginNetwork(Network originNetwork) {
        this.originNetwork = originNetwork;
    }

    public Network getDestinationNetwork() {
        return destinationNetwork;
    }

    public void setDestinationNetwork(Network destinationNetwork) {
        this.destinationNetwork = destinationNetwork;
    }
}
