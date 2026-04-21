package io.github.mgrtomaszzurawski.baselinker.client.inventory.products;

import io.github.mgrtomaszzurawski.baselinker.client.BaselinkerApiException;
import io.github.mgrtomaszzurawski.baselinker.client.BaselinkerClient;
import io.github.mgrtomaszzurawski.baselinker.client.TestBaselinkerClientFactory;
import io.github.mgrtomaszzurawski.baselinker.client.inventory.model.InventoryProduct;
import io.github.mgrtomaszzurawski.baselinker.client.inventory.model.InventoryProductData;
import io.github.mgrtomaszzurawski.baselinker.client.inventory.model.InventoryProductLog;
import io.github.mgrtomaszzurawski.baselinker.client.inventory.model.InventoryStockEntry;
import org.junit.jupiter.api.Test;

import java.net.URLDecoder;
import java.net.http.HttpRequest;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryProductsServiceTest {

    private static final long SAMPLE_INVENTORY_ID = 307L;
    private static final String PRODUCT_ID_A = "12345";
    private static final String PRODUCT_ID_B = "12346";
    private static final String PRODUCT_ID_C = "12347";
    private static final String WAREHOUSE_KEY_A = "bl_206";
    private static final String WAREHOUSE_KEY_B = "bl_207";
    private static final int STOCK_A = 5;
    private static final int STOCK_B = 7;
    private static final int UPDATED_COUNTER = 2;
    private static final String ERROR_BODY =
            "{\"status\":\"ERROR\",\"error_code\":\"ERROR_BAD\",\"error_message\":\"bad\"}";
    private static final String PARAMETERS_PREFIX = "parameters=";

    @Test
    void list_whenStreamed_autoPaginatesAndFlattensKeyedProducts() {
        AtomicInteger fetchCount = new AtomicInteger();
        BaselinkerClient client = TestBaselinkerClientFactory.withStubbedResponse(request -> {
            int call = fetchCount.incrementAndGet();
            if (call == 1) {
                return productsListBodyTwo();
            }
            if (call == 2) {
                return productsListBodyOne();
            }
            return "{\"status\":\"SUCCESS\",\"products\":{}}";
        });
        InventoryProductsService service = new InventoryProductsService(client);

        List<String> ids = service.list(
                GetInventoryProductsListRequest.of(SAMPLE_INVENTORY_ID)
        ).stream().map(InventoryProduct::id).toList();

        assertEquals(List.of(PRODUCT_ID_A, PRODUCT_ID_B, PRODUCT_ID_C), ids);
        assertEquals(3, fetchCount.get(), "two pages with data + empty stop");
    }

    @Test
    void list_whenFirstPageEmpty_returnsNoItems() {
        BaselinkerClient client = TestBaselinkerClientFactory.withStubbedResponse(
                request -> "{\"status\":\"SUCCESS\",\"products\":{}}");
        InventoryProductsService service = new InventoryProductsService(client);

        assertEquals(List.of(), service.list(
                GetInventoryProductsListRequest.of(SAMPLE_INVENTORY_ID)).toList());
    }

    @Test
    void list_whenRequestIsNull_throwsNullPointerException() {
        BaselinkerClient client = TestBaselinkerClientFactory.withStubbedResponse(
                request -> "{\"status\":\"SUCCESS\",\"products\":{}}");
        InventoryProductsService service = new InventoryProductsService(client);

        assertThrows(NullPointerException.class, () -> service.list(null));
    }

    @Test
    void data_whenCalled_returnsProductMapByProductId() {
        BaselinkerClient client = TestBaselinkerClientFactory.withStubbedResponse(
                request -> productsDataBody());
        InventoryProductsService service = new InventoryProductsService(client);

        Map<String, InventoryProductData> result = service.data(SAMPLE_INVENTORY_ID,
                List.of(PRODUCT_ID_A));

        assertEquals(1, result.size());
        InventoryProductData data = result.get(PRODUCT_ID_A);
        assertNotNull(data);
        assertEquals("ABC-123", data.sku());
    }

    @Test
    void data_whenProductIdsEmpty_throwsIllegalArgumentException() {
        BaselinkerClient client = TestBaselinkerClientFactory.withStubbedResponse(
                request -> "{\"status\":\"SUCCESS\"}");
        InventoryProductsService service = new InventoryProductsService(client);

        assertThrows(IllegalArgumentException.class,
                () -> service.data(SAMPLE_INVENTORY_ID, List.of()));
    }

    @Test
    void data_whenApiReturnsError_throwsBaselinkerApiException() {
        BaselinkerClient client = TestBaselinkerClientFactory.withStubbedResponse(
                request -> ERROR_BODY);
        InventoryProductsService service = new InventoryProductsService(client);

        assertThrows(BaselinkerApiException.class,
                () -> service.data(SAMPLE_INVENTORY_ID, List.of(PRODUCT_ID_A)));
    }

    @Test
    void stock_whenStreamed_flattensAndPaginates() {
        AtomicInteger fetchCount = new AtomicInteger();
        BaselinkerClient client = TestBaselinkerClientFactory.withStubbedResponse(request -> {
            int call = fetchCount.incrementAndGet();
            if (call == 1) {
                return stockBody();
            }
            return "{\"status\":\"SUCCESS\",\"products\":{}}";
        });
        InventoryProductsService service = new InventoryProductsService(client);

        List<InventoryStockEntry> entries = service.stock(SAMPLE_INVENTORY_ID).toList();

        assertEquals(1, entries.size());
        assertEquals(PRODUCT_ID_A, entries.get(0).productId());
        assertEquals(STOCK_A, entries.get(0).stockByWarehouse().get(WAREHOUSE_KEY_A));
    }

    @Test
    void updateStock_whenCalled_sendsMethodAndReturnsCounter() {
        AtomicReference<HttpRequest> captured = new AtomicReference<>();
        BaselinkerClient client = TestBaselinkerClientFactory.withStubbedResponses(
                captured, request -> updateStockBody());
        InventoryProductsService service = new InventoryProductsService(client);

        UpdateInventoryProductsStockResult result = service.updateStock(
                UpdateInventoryProductsStockRequest.of(SAMPLE_INVENTORY_ID,
                        Map.of(PRODUCT_ID_A, Map.of(WAREHOUSE_KEY_A, STOCK_A))));

        assertEquals(UPDATED_COUNTER, result.counter());
        String body = extractBody(captured.get());
        assertTrue(body.startsWith("method=updateInventoryProductsStock"),
                "body must begin with updateInventoryProductsStock: " + body);
    }

    @Test
    void updateStock_whenStockByProductEmpty_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> UpdateInventoryProductsStockRequest.of(
                SAMPLE_INVENTORY_ID, Map.of()));
    }

    @Test
    void logs_whenStreamed_autoPaginatesByPageNumber() {
        AtomicInteger fetchCount = new AtomicInteger();
        BaselinkerClient client = TestBaselinkerClientFactory.withStubbedResponse(request -> {
            int call = fetchCount.incrementAndGet();
            if (call == 1) {
                return logsBody();
            }
            return "{\"status\":\"SUCCESS\",\"logs\":[]}";
        });
        InventoryProductsService service = new InventoryProductsService(client);

        List<InventoryProductLog> logs = service.logs(
                GetInventoryProductLogsRequest.builder()
                        .productId(PRODUCT_ID_A)
                        .logType(InventoryLogType.STOCK)
                        .build()
        ).toList();

        assertEquals(1, logs.size());
        assertEquals("profile-1", logs.get(0).profile());
    }

    @Test
    void logs_whenRequestSendsLogType_sendsNumericWireValue() {
        AtomicReference<HttpRequest> captured = new AtomicReference<>();
        BaselinkerClient client = TestBaselinkerClientFactory.withStubbedResponses(
                captured, request -> "{\"status\":\"SUCCESS\",\"logs\":[]}");
        InventoryProductsService service = new InventoryProductsService(client);

        service.logs(GetInventoryProductLogsRequest.builder()
                .productId(PRODUCT_ID_A)
                .logType(InventoryLogType.PRICE)
                .build()).toList();

        String params = decodeParams(extractBody(captured.get()));
        assertTrue(params.contains("\"log_type\":" + InventoryLogType.PRICE.wireValue()),
                "params must contain numeric log_type: " + params);
    }

    @Test
    void constructor_whenClientIsNull_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new InventoryProductsService(null));
    }

    @Test
    void inventoryLogType_fromWireValue_whenUnknown_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> InventoryLogType.fromWireValue(999));
    }

    @Test
    void inventoryLogType_fromWireValue_whenKnown_returnsMatchingEnum() {
        assertEquals(InventoryLogType.STOCK,
                InventoryLogType.fromWireValue(InventoryLogType.STOCK.wireValue()));
    }

    @Test
    void getInventoryProductLogsRequestBuilder_whenProductIdMissing_throwsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> GetInventoryProductLogsRequest.builder().build());
    }

    private static String productsListBodyTwo() {
        return String.format("""
                {
                  "status": "SUCCESS",
                  "products": {
                    "%s": {"id": "%s", "sku": "A-1", "name": "Product A"},
                    "%s": {"id": "%s", "sku": "B-1", "name": "Product B"}
                  }
                }
                """, PRODUCT_ID_A, PRODUCT_ID_A, PRODUCT_ID_B, PRODUCT_ID_B);
    }

    private static String productsListBodyOne() {
        return String.format("""
                {
                  "status": "SUCCESS",
                  "products": {
                    "%s": {"id": "%s", "sku": "C-1", "name": "Product C"}
                  }
                }
                """, PRODUCT_ID_C, PRODUCT_ID_C);
    }

    private static String productsDataBody() {
        return String.format("""
                {
                  "status": "SUCCESS",
                  "products": {
                    "%s": {"sku": "ABC-123", "ean": "5901234567890", "is_bundle": false}
                  }
                }
                """, PRODUCT_ID_A);
    }

    private static String stockBody() {
        return String.format("""
                {
                  "status": "SUCCESS",
                  "products": {
                    "%s": {"product_id": "%s", "stock": {"%s": %d, "%s": %d}}
                  }
                }
                """, PRODUCT_ID_A, PRODUCT_ID_A, WAREHOUSE_KEY_A, STOCK_A, WAREHOUSE_KEY_B, STOCK_B);
    }

    private static String updateStockBody() {
        return String.format("""
                {"status": "SUCCESS", "counter": %d, "warnings": {}}
                """, UPDATED_COUNTER);
    }

    private static String logsBody() {
        return """
                {
                  "status": "SUCCESS",
                  "logs": [
                    {"profile": "profile-1", "date": 1700000000, "entries": [{"type": 1, "from": "5", "to": "10"}]}
                  ]
                }
                """;
    }

    private static String extractBody(HttpRequest request) {
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

    private static String decodeParams(String body) {
        int index = body.indexOf(PARAMETERS_PREFIX);
        if (index < 0) {
            return "";
        }
        return URLDecoder.decode(body.substring(index + PARAMETERS_PREFIX.length()),
                StandardCharsets.UTF_8);
    }
}
