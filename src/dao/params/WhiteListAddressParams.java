package dao.params;

public class WhiteListAddressParams {
    private String bscAddress;
    private String bankName;
    private String bankAccountNumber;

    public WhiteListAddressParams(String bscAddress, String bankName, String bankAccountNumber) {
        this.bscAddress = bscAddress;
        this.bankName = bankName;
        this.bankAccountNumber = bankAccountNumber;
    }

    public String getBscAddress() {
        return bscAddress;
    }

    public void setBscAddress(String bscAddress) {
        this.bscAddress = bscAddress;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankAccountNumber() {
        return bankAccountNumber;
    }

    public void setBankAccountNumber(String bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber;
    }
}
