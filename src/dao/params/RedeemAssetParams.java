package dao.params;

public class RedeemAssetParams {
    int amount;
    String bankCode;
    String accountNumber;
    boolean saveDetails;

    public RedeemAssetParams(int amount, String bankCode, String accountNumber, boolean saveDetails) {
        this.amount = amount;
        this.bankCode = bankCode;
        this.accountNumber = accountNumber;
        this.saveDetails = saveDetails;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public boolean getSaveDetails() {
        return saveDetails;
    }

    public void setSaveDetails(boolean saveDetails) {
        this.saveDetails = saveDetails;
    }
}
