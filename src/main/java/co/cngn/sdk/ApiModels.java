package co.cngn.sdk;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.util.List;

/** Response models. Unknown fields are ignored so API additions remain compatible. */
public final class ApiModels {
    private ApiModels() {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Balance(@JsonProperty("asset_type") String assetType,
                          @JsonProperty("asset_code") String assetCode,
                          BigDecimal balance) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Transaction(String id, @JsonProperty("from") String sender,
                              JsonNode receiver, BigDecimal amount, String description,
                              @JsonProperty("createdAt") String createdAt,
                              @JsonProperty("trx_ref") String trxRef,
                              @JsonProperty("trx_type") String trxType,
                              String network, @JsonProperty("asset_type") String assetType,
                              @JsonProperty("asset_symbol") String assetSymbol,
                              @JsonProperty("base_trx_hash") String baseTrxHash,
                              @JsonProperty("extl_trx_hash") String externalTrxHash,
                              @JsonProperty("explorer_link") String explorerLink,
                              String status) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Pagination(int count, int pages,
                             @JsonProperty("isLastPage") boolean lastPage,
                             @JsonProperty("nextPage") Integer nextPage,
                             @JsonProperty("previousPage") Integer previousPage) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TransactionPage(List<Transaction> data, Pagination pagination) {
        public TransactionPage {
            data = data == null ? List.of() : List.copyOf(data);
            pagination = pagination == null ? new Pagination(0, 0, true, null, null) : pagination;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Network(String id, String name, @JsonProperty("short_name") String shortName,
                          @JsonProperty("isDisabled") Boolean disabled, JsonNode blockchain) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record VirtualAccount(@JsonProperty("accountNumber") String accountNumber,
                                 @JsonProperty("accountName") String accountName,
                                 @JsonProperty("bankName") String bankName,
                                 @JsonProperty("bankCode") String bankCode) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TemporaryAccount(String reference,
                                   @JsonProperty("paymentReference") String paymentReference,
                                   BigDecimal amount,
                                   @JsonProperty("amountExpected") BigDecimal amountExpected,
                                   BigDecimal fee, BigDecimal vat, String currency, String status,
                                   String narration, @JsonProperty("accountNumber") String accountNumber,
                                   @JsonProperty("accountName") String accountName,
                                   @JsonProperty("bankName") String bankName,
                                   @JsonProperty("bankCode") String bankCode,
                                   @JsonProperty("expiresAt") String expiresAt) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Bank(String name, String code, @JsonProperty("bankCode") String bankCode) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TransferResult(@JsonProperty("trxRef") String trxRef, String address) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AccountVerification(@JsonProperty("accountName") String accountName,
                                      @JsonProperty("accountNumber") String accountNumber,
                                      @JsonProperty("bankCode") String bankCode) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record BridgeQuote(@JsonProperty("amountReceivable") BigDecimal amountReceivable,
                              @JsonProperty("networkFee") BigDecimal networkFee,
                              @JsonProperty("bridgeFee") BigDecimal bridgeFee) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record BridgeResult(@JsonProperty("receivableAddress") String receivableAddress,
                               @JsonProperty("transactionId") String transactionId,
                               String reference) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WhitelistEntry(String id, @JsonProperty("networkId") String networkId,
                                 @JsonProperty("publicKey") String publicKey,
                                 @JsonProperty("internalPublicKey") String internalPublicKey,
                                 JsonNode network,
                                 @JsonProperty("created_at") String createdAt,
                                 @JsonProperty("updated_at") String updatedAt) {}
}
