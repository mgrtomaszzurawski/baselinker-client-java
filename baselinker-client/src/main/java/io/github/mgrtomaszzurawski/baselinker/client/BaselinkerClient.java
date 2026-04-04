package io.github.mgrtomaszzurawski.baselinker.client;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    private static final ObjectMapper DEFAULT_OBJECT_MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    static final String STATUS_ERROR = "ERROR";
    static final String HEADER_TOKEN = "X-BLToken";
    static final String HEADER_CONTENT_TYPE = "Content-Type";
    static final String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";
    private static final String PARAM_METHOD = "method";
    private static final String PARAM_PARAMETERS = "parameters";
    private static final String METHOD_GET_STATUS = "getStatus";
    private static final String METHOD_GET_ERROR_CODE = "getErrorCode";
    private static final String METHOD_GET_ERROR_MESSAGE = "getErrorMessage";

    private final String apiToken;
    private final URI apiUrl;
    private final Duration requestTimeout;
    private final HttpTransport transport;
    private final ObjectMapper objectMapper;

    @FunctionalInterface
    interface HttpTransport {
        String send(HttpRequest request) throws IOException, InterruptedException;
    }

    public BaselinkerClient(String apiToken) {
        this(apiToken, URI.create(DEFAULT_API_URL), defaultHttpClient(), DEFAULT_OBJECT_MAPPER);
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
        try {
            Method getStatus = response.getClass().getMethod(METHOD_GET_STATUS);
            Object status = getStatus.invoke(response);
            if (status != null && STATUS_ERROR.equals(status.toString())) {
                String errorCode = invokeStringGetter(response, METHOD_GET_ERROR_CODE);
                String errorMessage = invokeStringGetter(response, METHOD_GET_ERROR_MESSAGE);
                throw new BaselinkerApiException(errorCode, errorMessage);
            }
        } catch (BaselinkerApiException exception) {
            throw exception;
        } catch (NoSuchMethodException exception) {
            // Model lacks getStatus() — skip error check, return as-is
        } catch (InvocationTargetException | IllegalAccessException exception) {
            throw new RuntimeException("Failed to check API error status", exception);
        }
    }

    private static String invokeStringGetter(Object target, String methodName) {
        try {
            Method getter = target.getClass().getMethod(methodName);
            Object value = getter.invoke(target);
            return value != null ? value.toString() : null;
        } catch (NoSuchMethodException exception) {
            return null;
        } catch (InvocationTargetException | IllegalAccessException exception) {
            throw new RuntimeException("Failed to invoke " + methodName, exception);
        }
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

    private static HttpClient defaultHttpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(DEFAULT_CONNECT_TIMEOUT)
                .build();
    }
}
