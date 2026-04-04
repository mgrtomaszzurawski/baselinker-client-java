package io.github.mgrtomaszzurawski.baselinker.client;

public class BaselinkerApiException extends BaselinkerException {

    private final String errorCode;

    public BaselinkerApiException(String errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
