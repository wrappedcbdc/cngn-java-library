package dao.params;

import org.json.JSONObject;

public class UpdateExternalAccountParams {

    private WalletAddress walletAddress;
    private BankDetails bankDetails;

    public UpdateExternalAccountParams(WalletAddress walletAddress, BankDetails bankDetails) {
        this.walletAddress = walletAddress;
        this.bankDetails = bankDetails;
    }

    public UpdateExternalAccountParams() {

    }

    public WalletAddress getWalletAddress() {
        return walletAddress;
    }

    public void setWalletAddress(WalletAddress walletAddress) {
        this.walletAddress = walletAddress;
    }

    public BankDetails getBankDetails() {
        return bankDetails;
    }

    public void setBankDetails(BankDetails bankDetails) {
        this.bankDetails = bankDetails;
    }

    public static class WalletAddress {
        private String address;

        public WalletAddress(String address) {
            this.address = address;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }
    }

    public static class BankDetails {
        private String bankName;
        private String bankAccountName;
        private String bankAccountNumber;

        public BankDetails(String bankName, String bankAccountName, String bankAccountNumber) {
            this.bankName = bankName;
            this.bankAccountName = bankAccountName;
            this.bankAccountNumber = bankAccountNumber;
        }

        // Getters and setters
        public String getBankName() {
            return bankName;
        }

        public void setBankName(String bankName) {
            this.bankName = bankName;
        }

        public String getBankAccountName() {
            return bankAccountName;
        }

        public void setBankAccountName(String bankAccountName) {
            this.bankAccountName = bankAccountName;
        }

        public String getBankAccountNumber() {
            return bankAccountNumber;
        }

        public void setBankAccountNumber(String bankAccountNumber) {
            this.bankAccountNumber = bankAccountNumber;
        }
    }
}
