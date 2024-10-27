package dao.params;

public class DepositParams {

    private int amount;
    private String bank;
    private String accountNumber;

    public DepositParams(int amount, String bank, String accountNumber) {
        this.amount = amount;
        this.bank = bank;
        this.accountNumber = accountNumber;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
}
