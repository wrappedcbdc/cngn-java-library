package dao.params;

public class CreateVirtualParams {

    String provider;
    String bankCode;

    public CreateVirtualParams(String provider, String bankCode) {
        this.provider = provider;
        this.bankCode = bankCode;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }
}
