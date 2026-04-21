package io.github.mgrtomaszzurawski.baselinker.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.mgrtomaszzurawski.baselinker.client.json.BaselinkerJacksonModule;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * Test-only helper exposing the package-private {@link BaselinkerClient.HttpTransport} seam
 * so service-level tests (outside the {@code client} package) can build a mock client
 * without any real network calls.
 */
public final class TestBaselinkerClientFactory {

    private static final String DEFAULT_TEST_TOKEN = "test-api-token-12345";
    private static final URI DEFAULT_TEST_URL = URI.create("https://api.baselinker.com/connector.php");

    private TestBaselinkerClientFactory() {
    }

    public static ObjectMapper defaultMapper() {
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .registerModule(new BaselinkerJacksonModule());
    }

    public static BaselinkerClient withStubbedResponse(Function<HttpRequest, String> responder) {
        return new BaselinkerClient(
                DEFAULT_TEST_TOKEN,
                DEFAULT_TEST_URL,
                request -> {
                    try {
                        return responder.apply(request);
                    } catch (RuntimeIoException wrapped) {
                        throw wrapped.delegate;
                    }
                },
                defaultMapper()
        );
    }

    public static BaselinkerClient withStubbedResponses(AtomicReference<HttpRequest> captured,
                                                        Function<HttpRequest, String> responder) {
        return new BaselinkerClient(
                DEFAULT_TEST_TOKEN,
                DEFAULT_TEST_URL,
                request -> {
                    captured.set(request);
                    return responder.apply(request);
                },
                defaultMapper()
        );
    }

    /**
     * Unchecked wrapper to pass {@link IOException} out of a {@link Function} lambda.
     * Unwrapped by {@link #withStubbedResponse} back into the checked transport signature.
     */
    public static final class RuntimeIoException extends RuntimeException {
        private final IOException delegate;

        public RuntimeIoException(IOException delegate) {
            super(delegate);
            this.delegate = delegate;
        }
    }
}
