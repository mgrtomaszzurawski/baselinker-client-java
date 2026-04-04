package io.github.mgrtomaszzurawski.baselinker.client.model;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ResponseDeserializationTest {

    private static ObjectMapper objectMapper;

    @BeforeAll
    static void setUp() {
        objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    static Stream<Arguments> responseFixtures() {
        return Stream.of(
                Arguments.of("getOrders.json", GetOrdersResponse.class),
                Arguments.of("addOrder.json", AddOrderResponse.class),
                Arguments.of("setOrderStatus.json", SetOrderStatusResponse.class),
                Arguments.of("getOrderStatusList.json", GetOrderStatusListResponse.class),
                Arguments.of("getInventoryProductsList.json", GetInventoryProductsListResponse.class),
                Arguments.of("getInventoryProductsData.json", GetInventoryProductsDataResponse.class),
                Arguments.of("getInventoryProductsStock.json", GetInventoryProductsStockResponse.class),
                Arguments.of("updateInventoryProductsStock.json", UpdateInventoryProductsStockResponse.class),
                Arguments.of("getInventoryProductLogs.json", GetInventoryProductLogsResponse.class),
                Arguments.of("getInventories.json", GetInventoriesResponse.class),
                Arguments.of("getInventoryWarehouses.json", GetInventoryWarehousesResponse.class),
                Arguments.of("addInventoryDocument.json", AddInventoryDocumentResponse.class),
                Arguments.of("addInventoryDocumentItems.json", AddInventoryDocumentItemsResponse.class),
                Arguments.of("setInventoryDocumentStatusConfirmed.json", SetInventoryDocumentStatusConfirmedResponse.class),
                Arguments.of("getInventoryDocumentSeries.json", GetInventoryDocumentSeriesResponse.class)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("responseFixtures")
    void shouldDeserializeSuccessResponse(String fixture, Class<?> responseClass) throws IOException {
        String json = loadFixture(fixture);

        Object response = objectMapper.readValue(json, responseClass);

        assertNotNull(response);
        var method = findGetter(responseClass, "getStatus");
        assertNotNull(method, "Response class must have getStatus()");
        try {
            Object status = method.invoke(response);
            assertNotNull(status, "status must not be null");
            assertEquals(ApiResponseBase.StatusEnum.SUCCESS.getValue(), status.toString());
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Failed to call getStatus()", exception);
        }
    }

    @Test
    void shouldDeserializeAddOrderFields() throws IOException {
        String json = loadFixture("addOrder.json");
        AddOrderResponse response = objectMapper.readValue(json, AddOrderResponse.class);
        assertEquals(16331079, response.getOrderId());
    }

    @Test
    void shouldDeserializeGetOrdersFields() throws IOException {
        String json = loadFixture("getOrders.json");
        GetOrdersResponse response = objectMapper.readValue(json, GetOrdersResponse.class);
        assertNotNull(response.getOrders());
        assertFalse(response.getOrders().isEmpty());
    }

    @Test
    void shouldDeserializeGetOrderStatusListFields() throws IOException {
        String json = loadFixture("getOrderStatusList.json");
        GetOrderStatusListResponse response = objectMapper.readValue(json, GetOrderStatusListResponse.class);
        assertNotNull(response.getStatuses());
        assertEquals(2, response.getStatuses().size());
    }

    @Test
    void shouldDeserializeAddInventoryDocumentFields() throws IOException {
        String json = loadFixture("addInventoryDocument.json");
        AddInventoryDocumentResponse response = objectMapper.readValue(json, AddInventoryDocumentResponse.class);
        assertEquals(101, response.getDocumentId());
        assertEquals("GR/2021/1", response.getDocumentNumber());
    }

    @Test
    void shouldDeserializeUpdateInventoryProductsStockFields() throws IOException {
        String json = loadFixture("updateInventoryProductsStock.json");
        UpdateInventoryProductsStockResponse response = objectMapper.readValue(json, UpdateInventoryProductsStockResponse.class);
        assertEquals(2, response.getCounter());
    }

    @Test
    void shouldDeserializeGetInventoryWarehousesFields() throws IOException {
        String json = loadFixture("getInventoryWarehouses.json");
        GetInventoryWarehousesResponse response = objectMapper.readValue(json, GetInventoryWarehousesResponse.class);
        assertNotNull(response.getWarehouses());
        assertEquals(2, response.getWarehouses().size());
    }

    private static String loadFixture(String filename) throws IOException {
        try (InputStream stream = ResponseDeserializationTest.class
                .getResourceAsStream("/fixtures/" + filename)) {
            assertNotNull(stream, "Fixture not found: " + filename);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static java.lang.reflect.Method findGetter(Class<?> clazz, String name) {
        try {
            return clazz.getMethod(name);
        } catch (NoSuchMethodException exception) {
            return null;
        }
    }
}
