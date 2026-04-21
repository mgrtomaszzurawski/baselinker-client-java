package io.github.mgrtomaszzurawski.baselinker.client.documents;

import io.github.mgrtomaszzurawski.baselinker.client.BaselinkerApiException;
import io.github.mgrtomaszzurawski.baselinker.client.BaselinkerClient;
import io.github.mgrtomaszzurawski.baselinker.client.TestBaselinkerClientFactory;
import io.github.mgrtomaszzurawski.baselinker.client.documents.model.InventoryDocumentSeries;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.http.HttpRequest;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryDocumentsServiceTest {

    private static final long SAMPLE_WAREHOUSE_ID = 205L;
    private static final long TARGET_WAREHOUSE_ID = 206L;
    private static final long CREATED_DOCUMENT_ID = 101L;
    private static final long SAMPLE_PRODUCT_ID = 5432L;
    private static final long CREATED_ITEM_ID = 1001L;
    private static final int SAMPLE_QUANTITY = 5;
    private static final long SAMPLE_SERIES_ID = 3L;
    private static final String SERIES_NAME = "GRN";
    private static final String SERIES_FORMAT = "%N/%M/%Y/GR";
    private static final BigDecimal SAMPLE_PRICE = new BigDecimal("10.99");
    private static final String ERROR_BODY =
            "{\"status\":\"ERROR\",\"error_code\":\"ERROR_BAD\",\"error_message\":\"bad\"}";
    private static final String PARAMETERS_PREFIX = "parameters=";

    @Test
    void add_whenCalled_sendsMethodAndReturnsDocumentId() {
        AtomicReference<HttpRequest> captured = new AtomicReference<>();
        BaselinkerClient client = TestBaselinkerClientFactory.withStubbedResponses(
                captured, request -> addDocumentBody());
        InventoryDocumentsService service = new InventoryDocumentsService(client);

        AddInventoryDocumentResult result = service.add(AddInventoryDocumentRequest.builder()
                .warehouseId(SAMPLE_WAREHOUSE_ID)
                .documentType(InventoryDocumentType.GOODS_RECEIPT)
                .notes("test")
                .build());

        assertEquals(CREATED_DOCUMENT_ID, result.documentId());
        assertEquals("GR/2021/1", result.documentNumber());
        assertTrue(extractBody(captured.get()).startsWith("method=addInventoryDocument&"),
                "must send addInventoryDocument method");
    }

    @Test
    void add_whenDocumentTypeMissing_throwsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> AddInventoryDocumentRequest.builder().warehouseId(SAMPLE_WAREHOUSE_ID).build());
    }

    @Test
    void add_whenApiReturnsError_throwsBaselinkerApiException() {
        BaselinkerClient client = TestBaselinkerClientFactory.withStubbedResponse(
                request -> ERROR_BODY);
        InventoryDocumentsService service = new InventoryDocumentsService(client);

        assertThrows(BaselinkerApiException.class,
                () -> service.add(AddInventoryDocumentRequest.builder()
                        .warehouseId(SAMPLE_WAREHOUSE_ID)
                        .documentType(InventoryDocumentType.GOODS_RECEIPT)
                        .build()));
    }

    @Test
    void addItems_whenCalled_sendsDocumentIdAndItemList() {
        AtomicReference<HttpRequest> captured = new AtomicReference<>();
        BaselinkerClient client = TestBaselinkerClientFactory.withStubbedResponses(
                captured, request -> addItemsBody());
        InventoryDocumentsService service = new InventoryDocumentsService(client);

        AddInventoryDocumentItemsResult result = service.addItems(
                CREATED_DOCUMENT_ID,
                List.of(InventoryDocumentItem.builder()
                        .productId(SAMPLE_PRODUCT_ID)
                        .quantity(SAMPLE_QUANTITY)
                        .price(SAMPLE_PRICE)
                        .build()));

        assertEquals(1, result.itemsOrEmpty().size());
        assertEquals(CREATED_ITEM_ID, result.itemsOrEmpty().get(0).itemId());
        String params = decodeParams(extractBody(captured.get()));
        assertTrue(params.contains("\"document_id\":" + CREATED_DOCUMENT_ID),
                "params must contain document_id: " + params);
    }

    @Test
    void addItems_whenItemsEmpty_throwsIllegalArgumentException() {
        BaselinkerClient client = TestBaselinkerClientFactory.withStubbedResponse(
                request -> "{\"status\":\"SUCCESS\"}");
        InventoryDocumentsService service = new InventoryDocumentsService(client);

        assertThrows(IllegalArgumentException.class,
                () -> service.addItems(CREATED_DOCUMENT_ID, List.of()));
    }

    @Test
    void inventoryDocumentItemBuilder_whenQuantityNotPositive_throwsIllegalArgumentException() {
        InventoryDocumentItem.Builder builder = InventoryDocumentItem.builder()
                .productId(SAMPLE_PRODUCT_ID)
                .quantity(0);

        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    void confirm_whenCalled_sendsDocumentIdParam() {
        AtomicReference<HttpRequest> captured = new AtomicReference<>();
        BaselinkerClient client = TestBaselinkerClientFactory.withStubbedResponses(
                captured, request -> "{\"status\":\"SUCCESS\"}");
        InventoryDocumentsService service = new InventoryDocumentsService(client);

        service.confirm(CREATED_DOCUMENT_ID);

        String params = decodeParams(extractBody(captured.get()));
        assertTrue(params.contains("\"document_id\":" + CREATED_DOCUMENT_ID),
                "params must contain document_id: " + params);
    }

    @Test
    void confirm_whenApiReturnsError_throwsBaselinkerApiException() {
        BaselinkerClient client = TestBaselinkerClientFactory.withStubbedResponse(
                request -> ERROR_BODY);
        InventoryDocumentsService service = new InventoryDocumentsService(client);

        assertThrows(BaselinkerApiException.class,
                () -> service.confirm(CREATED_DOCUMENT_ID));
    }

    @Test
    void series_whenCalled_returnsParsedSeriesWithTypedEnum() {
        BaselinkerClient client = TestBaselinkerClientFactory.withStubbedResponse(
                request -> seriesBody());
        InventoryDocumentsService service = new InventoryDocumentsService(client);

        List<InventoryDocumentSeries> seriesList = service.series();

        assertEquals(1, seriesList.size());
        assertEquals(SAMPLE_SERIES_ID, seriesList.get(0).documentSeriesId());
        assertEquals(SERIES_NAME, seriesList.get(0).name());
        assertEquals(InventoryDocumentType.INTERNAL_GOODS_RECEIPT, seriesList.get(0).type());
        assertEquals(SAMPLE_WAREHOUSE_ID, seriesList.get(0).warehouseId());
        assertEquals(SERIES_FORMAT, seriesList.get(0).format());
    }

    @Test
    void series_whenApiReturnsEmptyList_returnsEmptyList() {
        BaselinkerClient client = TestBaselinkerClientFactory.withStubbedResponse(
                request -> "{\"status\":\"SUCCESS\"}");
        InventoryDocumentsService service = new InventoryDocumentsService(client);

        assertEquals(List.of(), service.series());
    }

    @Test
    void constructor_whenClientIsNull_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new InventoryDocumentsService(null));
    }

    @Test
    void inventoryDocumentType_fromWireValue_whenUnknown_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> InventoryDocumentType.fromWireValue(99));
    }

    @Test
    void inventoryDocumentType_fromWireValue_whenKnown_returnsMatchingEnum() {
        assertEquals(InventoryDocumentType.INTERNAL_TRANSFER,
                InventoryDocumentType.fromWireValue(InventoryDocumentType.INTERNAL_TRANSFER.wireValue()));
    }

    @Test
    void addInventoryDocumentRequestToParams_whenTargetWarehouseIdSet_includesInParams() {
        AddInventoryDocumentRequest request = AddInventoryDocumentRequest.builder()
                .warehouseId(SAMPLE_WAREHOUSE_ID)
                .documentType(InventoryDocumentType.INTERNAL_TRANSFER)
                .targetWarehouseId(TARGET_WAREHOUSE_ID)
                .build();

        assertEquals(TARGET_WAREHOUSE_ID, request.toParams().get("target_warehouse_id"));
    }

    private static String addDocumentBody() {
        return String.format("""
                {"status": "SUCCESS", "document_id": %d, "document_number": "GR/2021/1"}
                """, CREATED_DOCUMENT_ID);
    }

    private static String addItemsBody() {
        return String.format("""
                {"status": "SUCCESS", "items": [{"item_id": %d}]}
                """, CREATED_ITEM_ID);
    }

    private static String seriesBody() {
        return String.format("""
                {
                  "status": "SUCCESS",
                  "document_series": [
                    {"document_series_id": %d, "name": "%s", "type": %d, "warehouse_id": %d, "format": "%s"}
                  ]
                }
                """, SAMPLE_SERIES_ID, SERIES_NAME,
                InventoryDocumentType.INTERNAL_GOODS_RECEIPT.wireValue(),
                SAMPLE_WAREHOUSE_ID, SERIES_FORMAT);
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
