package io.github.mgrtomaszzurawski.baselinker.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

public class BaselinkerClient {

    private static final String DEFAULT_API_URL = "https://api.baselinker.com/connector.php";
    private static final String STATUS_ERROR = "ERROR";
    private static final String HEADER_TOKEN = "X-BLToken";
    private static final String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";
    private static final String PARAM_METHOD = "method";
    private static final String PARAM_PARAMETERS = "parameters";
    private static final String FIELD_STATUS = "status";
    private static final String FIELD_ERROR_CODE = "error_code";
    private static final String FIELD_ERROR_MESSAGE = "error_message";

    private final String apiToken;
    private final URI apiUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public BaselinkerClient(String apiToken) {
        this(apiToken, URI.create(DEFAULT_API_URL), HttpClient.newHttpClient(), defaultObjectMapper());
    }

    public BaselinkerClient(String apiToken, URI apiUrl, HttpClient httpClient, ObjectMapper objectMapper) {
        this.apiToken = Objects.requireNonNull(apiToken, "apiToken must not be null");
        this.apiUrl = Objects.requireNonNull(apiUrl, "apiUrl must not be null");
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient must not be null");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper must not be null");
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
                    .header(HEADER_TOKEN, apiToken)
                    .header("Content-Type", CONTENT_TYPE_FORM)
                    .POST(HttpRequest.BodyPublishers.ofString(formBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return parseResponse(response.body(), responseType);
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
            JsonNode root = objectMapper.readTree(body);
            JsonNode statusNode = root.get(FIELD_STATUS);
            if (statusNode != null && STATUS_ERROR.equals(statusNode.asText())) {
                String errorCode = root.has(FIELD_ERROR_CODE) ? root.get(FIELD_ERROR_CODE).asText() : null;
                String errorMessage = root.has(FIELD_ERROR_MESSAGE) ? root.get(FIELD_ERROR_MESSAGE).asText() : null;
                throw new BaselinkerApiException(errorCode, errorMessage);
            }
            return objectMapper.treeToValue(root, responseType);
        } catch (BaselinkerApiException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BaselinkerException("Failed to parse API response", exception);
        }
    }

    private static String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static ObjectMapper defaultObjectMapper() {
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
}
