package dao.params;

import util.Network;

public class SwapParams {
    private Network originatorNetwork;
    private String destinationAddress;
    private Network destinantionNetwork;
    private String callBackUrl;

    public SwapParams(Network originatorNetwork, String destinationAddress, Network destinantionNetwork, String callBackUrl) {
        this.originatorNetwork = originatorNetwork;
        this.destinationAddress = destinationAddress;
        this.destinantionNetwork = destinantionNetwork;
        this.callBackUrl = callBackUrl;
    }

    public Network getOriginatorNetwork() {
        return originatorNetwork;
    }

    public void setOriginatorNetwork(Network originatorNetwork) {
        this.originatorNetwork = originatorNetwork;
    }

    public String getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(String destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public Network getDestinantionNetwork() {
        return destinantionNetwork;
    }

    public void setDestinantionNetwork(Network destinantionNetwork) {
        this.destinantionNetwork = destinantionNetwork;
    }

    public String getCallBackUrl() {
        return callBackUrl;
    }

    public void setCallBackUrl(String callBackUrl) {
        this.callBackUrl = callBackUrl;
    }
}
