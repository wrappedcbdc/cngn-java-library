package co.cngn.sdk;

import com.fasterxml.jackson.databind.JsonNode;

/** Failure returned by the API or by the local transport/cryptography layer. */
public class CngnException extends RuntimeException {
    public enum Kind { CONFIGURATION, AUTHENTICATION, IP_ALLOWLIST, PERMISSION, RATE_LIMIT,
        VALIDATION, NOT_FOUND, SERVICE_UNAVAILABLE, API, NETWORK, ENCRYPTION, DECRYPTION }

    private final Kind kind;
    private final Integer httpStatus;
    private final JsonNode response;

    public CngnException(Kind kind, String message) { this(kind, message, null, null, null); }

    public CngnException(Kind kind, String message, Throwable cause) {
        this(kind, message, null, null, cause);
    }

    public CngnException(Kind kind, String message, Integer httpStatus, JsonNode response) {
        this(kind, message, httpStatus, response, null);
    }

    public CngnException(Kind kind, String message, Integer httpStatus, JsonNode response,
                         Throwable cause) {
        super(message, cause);
        this.kind = kind;
        this.httpStatus = httpStatus;
        this.response = response;
    }

    public Kind kind() { return kind; }
    public Integer httpStatus() { return httpStatus; }
    public JsonNode response() { return response; }
}
