package io.github.mgrtomaszzurawski.baselinker.client;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.github.mgrtomaszzurawski.baselinker.client.documents.InventoryDocumentsService;
import io.github.mgrtomaszzurawski.baselinker.client.inventory.InventoryService;
import io.github.mgrtomaszzurawski.baselinker.client.json.BaselinkerJacksonModule;
import io.github.mgrtomaszzurawski.baselinker.client.orders.OrdersService;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

public class BaselinkerClient {

    private static final String DEFAULT_API_URL = "https://api.baselinker.com/connector.php";
    private static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.ofSeconds(30);
    private static final ObjectMapper DEFAULT_OBJECT_MAPPER = defaultObjectMapper();
    private static final HttpClient DEFAULT_HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(DEFAULT_CONNECT_TIMEOUT)
            .build();

    static final String STATUS_ERROR = "ERROR";
    static final String HEADER_TOKEN = "X-BLToken";
    static final String HEADER_CONTENT_TYPE = "Content-Type";
    static final String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";
    private static final String PARAM_METHOD = "method";
    private static final String PARAM_PARAMETERS = "parameters";
    private static final String[] STATUS_ACCESSOR_NAMES = {"status", "getStatus"};
    private static final String[] ERROR_CODE_ACCESSOR_NAMES = {"errorCode", "getErrorCode"};
    private static final String[] ERROR_MESSAGE_ACCESSOR_NAMES = {"errorMessage", "getErrorMessage"};

    private final String apiToken;
    private final URI apiUrl;
    private final Duration requestTimeout;
    private final HttpTransport transport;
    private final ObjectMapper objectMapper;
    private final OrdersService orders;
    private final InventoryService inventory;
    private final InventoryDocumentsService inventoryDocuments;

    @FunctionalInterface
    interface HttpTransport {
        String send(HttpRequest request) throws IOException, InterruptedException;
    }

    public static Builder builder() {
        return new Builder();
    }

    public BaselinkerClient(String apiToken) {
        this(apiToken, URI.create(DEFAULT_API_URL), DEFAULT_HTTP_CLIENT, DEFAULT_OBJECT_MAPPER);
    }

    public BaselinkerClient(String apiToken, URI apiUrl, HttpClient httpClient, ObjectMapper objectMapper) {
        this(
                apiToken,
                apiUrl,
                Objects.requireNonNull(httpClient, "httpClient must not be null"),
                objectMapper,
                DEFAULT_REQUEST_TIMEOUT
        );
    }

    private BaselinkerClient(String apiToken, URI apiUrl, HttpClient httpClient,
                             ObjectMapper objectMapper, Duration requestTimeout) {
        this(
                apiToken,
                apiUrl,
                request -> httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body(),
                objectMapper,
                requestTimeout
        );
    }

    BaselinkerClient(String apiToken, URI apiUrl, HttpTransport transport, ObjectMapper objectMapper) {
        this(apiToken, apiUrl, transport, objectMapper, DEFAULT_REQUEST_TIMEOUT);
    }

    BaselinkerClient(String apiToken, URI apiUrl, HttpTransport transport,
                     ObjectMapper objectMapper, Duration requestTimeout) {
        this.apiToken = requireNonBlank(apiToken, "apiToken must not be null or blank");
        this.apiUrl = Objects.requireNonNull(apiUrl, "apiUrl must not be null");
        this.transport = Objects.requireNonNull(transport, "transport must not be null");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper must not be null");
        this.requestTimeout = Objects.requireNonNull(requestTimeout, "requestTimeout must not be null");
        this.orders = new OrdersService(this);
        this.inventory = new InventoryService(this);
        this.inventoryDocuments = new InventoryDocumentsService(this);
    }

    /**
     * Orders service — typed access to order-related BaseLinker methods.
     */
    public OrdersService orders() {
        return orders;
    }

    /**
     * Inventory service — typed access to catalogs, warehouses, and products.
     */
    public InventoryService inventory() {
        return inventory;
    }

    /**
     * Inventory documents service — typed access to the GRN (goods receipt) flow and
     * related document operations.
     */
    public InventoryDocumentsService inventoryDocuments() {
        return inventoryDocuments;
    }

    public <T> T execute(String method, Map<String, Object> parameters, Class<T> responseType)
            throws BaselinkerException {
        Objects.requireNonNull(method, "method must not be null");
        Objects.requireNonNull(responseType, "responseType must not be null");

        try {
            String parametersJson = objectMapper.writeValueAsString(
                    parameters != null ? parameters : Map.of());
            String formBody = PARAM_METHOD + "=" + urlEncode(method)
                    + "&" + PARAM_PARAMETERS + "=" + urlEncode(parametersJson);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(apiUrl)
                    .timeout(requestTimeout)
                    .header(HEADER_TOKEN, apiToken)
                    .header(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM)
                    .POST(HttpRequest.BodyPublishers.ofString(formBody))
                    .build();

            String responseBody = transport.send(request);
            return parseResponse(responseBody, responseType);
        } catch (BaselinkerException exception) {
            throw exception;
        } catch (IOException exception) {
            throw new BaselinkerException("API communication error", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BaselinkerException("API call interrupted", exception);
        }
    }

    public <T> T execute(String method, Class<T> responseType) throws BaselinkerException {
        return execute(method, null, responseType);
    }

    private <T> T parseResponse(String body, Class<T> responseType) throws BaselinkerException {
        try {
            T response = objectMapper.readValue(body, responseType);
            checkForApiError(response);
            return response;
        } catch (BaselinkerApiException exception) {
            throw exception;
        } catch (JacksonException exception) {
            throw new BaselinkerException("Failed to parse API response", exception);
        }
    }

    private <T> void checkForApiError(T response) throws BaselinkerApiException {
        Object status = invokeFirstAvailable(response, STATUS_ACCESSOR_NAMES);
        if (status == null || !STATUS_ERROR.equals(status.toString())) {
            return;
        }
        Object errorCode = invokeFirstAvailable(response, ERROR_CODE_ACCESSOR_NAMES);
        Object errorMessage = invokeFirstAvailable(response, ERROR_MESSAGE_ACCESSOR_NAMES);
        throw new BaselinkerApiException(
                errorCode != null ? errorCode.toString() : null,
                errorMessage != null ? errorMessage.toString() : null
        );
    }

    private static Object invokeFirstAvailable(Object target, String... methodNames) {
        for (String methodName : methodNames) {
            try {
                Method getter = target.getClass().getMethod(methodName);
                getter.setAccessible(true);
                return getter.invoke(target);
            } catch (NoSuchMethodException ignored) {
                // try next candidate
            } catch (InvocationTargetException | IllegalAccessException exception) {
                throw new IllegalStateException("Failed to invoke " + methodName, exception);
            }
        }
        return null;
    }

    private static String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String requireNonBlank(String value, String message) {
        Objects.requireNonNull(value, message);
        if (value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    private static ObjectMapper defaultObjectMapper() {
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setSerializationInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .registerModule(new BaselinkerJacksonModule());
    }

    /**
     * Fluent builder for {@link BaselinkerClient}. Only {@code apiToken} is required.
     */
    public static final class Builder {
        private String apiToken;
        private URI apiUrl = URI.create(DEFAULT_API_URL);
        private HttpClient httpClient = DEFAULT_HTTP_CLIENT;
        private ObjectMapper objectMapper = DEFAULT_OBJECT_MAPPER;
        private Duration requestTimeout = DEFAULT_REQUEST_TIMEOUT;

        private Builder() {
        }

        public Builder apiToken(String apiToken) {
            this.apiToken = apiToken;
            return this;
        }

        public Builder apiUrl(URI apiUrl) {
            this.apiUrl = Objects.requireNonNull(apiUrl, "apiUrl must not be null");
            return this;
        }

        public Builder httpClient(HttpClient httpClient) {
            this.httpClient = Objects.requireNonNull(httpClient, "httpClient must not be null");
            return this;
        }

        public Builder objectMapper(ObjectMapper objectMapper) {
            this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper must not be null");
            return this;
        }

        public Builder requestTimeout(Duration requestTimeout) {
            this.requestTimeout = Objects.requireNonNull(requestTimeout, "requestTimeout must not be null");
            return this;
        }

        public BaselinkerClient build() {
            return new BaselinkerClient(apiToken, apiUrl, httpClient, objectMapper, requestTimeout);
        }
    }
}
