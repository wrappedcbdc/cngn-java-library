package dao.params;

public class CreateVirtualAccount {

    String provider;
    String bankCode;

    public CreateVirtualAccount(String provider, String bankCode) {
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
