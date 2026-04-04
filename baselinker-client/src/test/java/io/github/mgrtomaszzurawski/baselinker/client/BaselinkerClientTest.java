package io.github.mgrtomaszzurawski.baselinker.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mgrtomaszzurawski.baselinker.client.model.AddOrderResponse;
import io.github.mgrtomaszzurawski.baselinker.client.model.GetOrdersResponse;
import io.github.mgrtomaszzurawski.baselinker.client.model.SetOrderStatusResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.net.http.HttpRequest;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BaselinkerClientTest {

    private static final String TEST_TOKEN = "test-api-token-12345";
    private static final URI TEST_URL = URI.create("https://api.baselinker.com/connector.php");

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Test
    void shouldSendCorrectRequestHeaders() throws Exception {
        AtomicReference<HttpRequest> captured = new AtomicReference<>();
        BaselinkerClient client = clientWithCapture(captured, loadFixture("addOrder.json"));

        client.execute("addOrder", Map.of("order_status_id", 1051), AddOrderResponse.class);

        HttpRequest request = captured.get();
        assertNotNull(request);
        assertEquals(TEST_URL, request.uri());
        assertEquals("POST", request.method());
        assertEquals(TEST_TOKEN,
                request.headers().firstValue(BaselinkerClient.HEADER_TOKEN).orElse(null));
        assertEquals(BaselinkerClient.CONTENT_TYPE_FORM,
                request.headers().firstValue(BaselinkerClient.HEADER_CONTENT_TYPE).orElse(null));
    }

    @Test
    void shouldEncodeMethodAndParametersInBody() throws Exception {
        AtomicReference<HttpRequest> captured = new AtomicReference<>();
        BaselinkerClient client = clientWithCapture(captured, loadFixture("addOrder.json"));

        client.execute("addOrder", Map.of("order_status_id", 1051), AddOrderResponse.class);

        String body = extractRequestBody(captured.get());
        assertTrue(body.startsWith("method=addOrder&parameters="));
        String encodedParams = body.substring("method=addOrder&parameters=".length());
        String decodedParams = URLDecoder.decode(encodedParams, StandardCharsets.UTF_8);
        assertTrue(decodedParams.contains("order_status_id"));
        assertTrue(decodedParams.contains("1051"));
    }

    @Test
    void shouldDeserializeSuccessResponse() throws Exception {
        BaselinkerClient client = clientWithResponse(loadFixture("addOrder.json"));

        AddOrderResponse response = client.execute("addOrder", Map.of(), AddOrderResponse.class);

        assertNotNull(response);
        assertEquals(AddOrderResponse.StatusEnum.SUCCESS, response.getStatus());
        assertEquals(16331079, response.getOrderId());
    }

    @Test
    void shouldThrowApiExceptionOnErrorResponse() {
        BaselinkerClient client = clientWithResponse(loadFixture("errorResponse.json"));

        BaselinkerApiException exception = assertThrows(BaselinkerApiException.class,
                () -> client.execute("getOrders", GetOrdersResponse.class));

        assertEquals("ERROR_EMPTY_TOKEN", exception.getErrorCode());
        assertEquals("Token is missing or invalid", exception.getMessage());
    }

    @Test
    void shouldSendEmptyParametersWhenNoneProvided() throws Exception {
        AtomicReference<HttpRequest> captured = new AtomicReference<>();
        BaselinkerClient client = clientWithCapture(captured, loadFixture("setOrderStatus.json"));

        client.execute("getOrderStatusList", SetOrderStatusResponse.class);

        String body = extractRequestBody(captured.get());
        assertTrue(body.contains("method=getOrderStatusList"));
        String encodedParams = body.split("parameters=")[1];
        String decodedParams = URLDecoder.decode(encodedParams, StandardCharsets.UTF_8);
        assertEquals("{}", decodedParams);
    }

    @Test
    void shouldRejectNullToken() {
        assertThrows(NullPointerException.class,
                () -> new BaselinkerClient(null));
    }

    @Test
    void shouldRejectBlankToken() {
        assertThrows(IllegalArgumentException.class,
                () -> new BaselinkerClient("   "));
    }

    @Test
    void shouldRejectEmptyToken() {
        assertThrows(IllegalArgumentException.class,
                () -> new BaselinkerClient(""));
    }

    @Test
    void shouldSetRequestTimeout() throws Exception {
        AtomicReference<HttpRequest> captured = new AtomicReference<>();
        BaselinkerClient client = clientWithCapture(captured, loadFixture("addOrder.json"));

        client.execute("addOrder", Map.of(), AddOrderResponse.class);

        assertTrue(captured.get().timeout().isPresent(), "Request should have a timeout");
    }

    @Test
    void shouldRejectNullMethod() {
        BaselinkerClient client = clientWithResponse("{}");

        assertThrows(NullPointerException.class,
                () -> client.execute(null, GetOrdersResponse.class));
    }

    @Test
    void shouldWrapIOExceptionAsBaselinkerException() {
        BaselinkerClient.HttpTransport failingTransport = request -> {
            throw new IOException("Connection refused");
        };
        BaselinkerClient client = new BaselinkerClient(TEST_TOKEN, TEST_URL, failingTransport, objectMapper);

        BaselinkerException exception = assertThrows(BaselinkerException.class,
                () -> client.execute("getOrders", GetOrdersResponse.class));
        assertEquals("API communication error", exception.getMessage());
        assertTrue(exception.getCause() instanceof IOException);
    }

    @Test
    void shouldWrapMalformedJsonAsBaselinkerException() {
        BaselinkerClient client = clientWithResponse("not valid json");

        BaselinkerException exception = assertThrows(BaselinkerException.class,
                () -> client.execute("getOrders", GetOrdersResponse.class));
        assertEquals("Failed to parse API response", exception.getMessage());
    }

    private BaselinkerClient clientWithResponse(String responseBody) {
        return new BaselinkerClient(TEST_TOKEN, TEST_URL, request -> responseBody, objectMapper);
    }

    private BaselinkerClient clientWithCapture(AtomicReference<HttpRequest> captured, String responseBody) {
        return new BaselinkerClient(TEST_TOKEN, TEST_URL, request -> {
            captured.set(request);
            return responseBody;
        }, objectMapper);
    }

    private static String loadFixture(String filename) {
        try (InputStream stream = BaselinkerClientTest.class
                .getResourceAsStream("/fixtures/" + filename)) {
            assertNotNull(stream, "Fixture not found: " + filename);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new AssertionError("Failed to load fixture: " + filename, exception);
        }
    }

    private static String extractRequestBody(HttpRequest request) {
        return request.bodyPublisher()
                .map(publisher -> {
                    CompletableFuture<String> future = new CompletableFuture<>();
                    StringBuilder builder = new StringBuilder();
                    publisher.subscribe(new Flow.Subscriber<>() {
                        @Override
                        public void onSubscribe(Flow.Subscription subscription) {
                            subscription.request(Long.MAX_VALUE);
                        }

                        @Override
                        public void onNext(ByteBuffer item) {
                            builder.append(StandardCharsets.UTF_8.decode(item));
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            future.completeExceptionally(throwable);
                        }

                        @Override
                        public void onComplete() {
                            future.complete(builder.toString());
                        }
                    });
                    return future.join();
                })
                .orElse("");
    }
}
