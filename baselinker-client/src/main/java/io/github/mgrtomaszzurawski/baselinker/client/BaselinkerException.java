package io.github.mgrtomaszzurawski.baselinker.client;

public class BaselinkerException extends Exception {

    public BaselinkerException(String message) {
        super(message);
    }

    public BaselinkerException(String message, Throwable cause) {
        super(message, cause);
    }
}
