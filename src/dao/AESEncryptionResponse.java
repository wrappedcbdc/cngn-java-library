package dao;

public class AESEncryptionResponse {
    private String iv;
    private String content;

    public AESEncryptionResponse(String content, String iv) {
        this.iv = iv;
        this.content = content;
    }

    public String getIv() {
        return iv;
    }

    public String getContent() {
        return content;
    }
}