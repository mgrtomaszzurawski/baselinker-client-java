package io.github.mgrtomaszzurawski.baselinker.client.orders;

import io.github.mgrtomaszzurawski.baselinker.client.BaselinkerApiException;
import io.github.mgrtomaszzurawski.baselinker.client.BaselinkerClient;
import io.github.mgrtomaszzurawski.baselinker.client.TestBaselinkerClientFactory;
import io.github.mgrtomaszzurawski.baselinker.client.model.AddOrderResponse;
import io.github.mgrtomaszzurawski.baselinker.client.orders.model.Order;
import io.github.mgrtomaszzurawski.baselinker.client.orders.model.OrderStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.http.HttpRequest;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrdersServiceTest {

    private static final long INITIAL_CURSOR_EPOCH_SECONDS = 1_700_000_000L;
    private static final Instant INITIAL_CURSOR = Instant.ofEpochSecond(INITIAL_CURSOR_EPOCH_SECONDS);
    private static final long CURSOR_INCREMENT_SECONDS = 1L;
    private static final long FIRST_BATCH_MAX_DATE_OFFSET = 10L;
    private static final long SECOND_BATCH_DATE_OFFSET = 50L;
    private static final long ORDER_ID_FIRST = 101L;
    private static final long ORDER_ID_SECOND = 102L;
    private static final long ORDER_ID_THIRD = 201L;
    private static final long SAMPLE_ORDER_STATUS_ID = 1051L;
    private static final long NEW_ORDER_ID = 16_331_079L;
    private static final String EMPTY_ORDERS_BODY = "{\"status\":\"SUCCESS\",\"orders\":[]}";
    private static final String ERROR_TOKEN_BODY =
            "{\"status\":\"ERROR\",\"error_code\":\"ERROR_EMPTY_TOKEN\",\"error_message\":\"Token is missing\"}";
    private static final String METHOD_PREFIX = "method=";
    private static final String PARAMETERS_PREFIX = "parameters=";

    @Test
    void list_whenStreamed_autoPaginatesUsingCursor() {
        AtomicInteger fetchCount = new AtomicInteger();
        BaselinkerClient client = TestBaselinkerClientFactory.withStubbedResponse(request -> {
            int call = fetchCount.incrementAndGet();
            if (call == 1) {
                return firstBatchBody();
            }
            if (call == 2) {
                return secondBatchBody();
            }
            return EMPTY_ORDERS_BODY;
        });
        OrdersService service = new OrdersService(client);

        List<Order> allOrders = service.list(
                GetOrdersRequest.builder().dateConfirmedFrom(INITIAL_CURSOR).build()
        ).toList();

        List<Long> ids = allOrders.stream().map(Order::orderId).toList();
        assertEquals(List.of(ORDER_ID_FIRST, ORDER_ID_SECOND, ORDER_ID_THIRD), ids);
        assertEquals(3, fetchCount.get(), "first batch + second batch + empty stop");
    }

    @Test
    void list_whenFirstBatchEmpty_returnsNoItems() {
        BaselinkerClient client = TestBaselinkerClientFactory.withStubbedResponse(
                request -> EMPTY_ORDERS_BODY);
        OrdersService service = new OrdersService(client);

        List<Order> orders = service.list(
                GetOrdersRequest.builder().dateConfirmedFrom(INITIAL_CURSOR).build()
        ).toList();

        assertEquals(List.of(), orders);
    }

    @Test
    void list_whenRequestIsNull_throwsNullPointerException() {
        BaselinkerClient client = TestBaselinkerClientFactory.withStubbedResponse(
                request -> EMPTY_ORDERS_BODY);
        OrdersService service = new OrdersService(client);

        assertThrows(NullPointerException.class, () -> service.list(null));
    }

    @Test
    void listRaw_whenCalled_returnsSingleBatch() {
        BaselinkerClient client = TestBaselinkerClientFactory.withStubbedResponse(
                request -> firstBatchBody());
        OrdersService service = new OrdersService(client);

        List<Order> orders = service.listRaw(
                GetOrdersRequest.builder().dateConfirmedFrom(INITIAL_CURSOR).build());

        List<Long> ids = orders.stream().map(Order::orderId).toList();
        assertEquals(List.of(ORDER_ID_FIRST, ORDER_ID_SECOND), ids);
    }

    @Test
    void listRaw_whenApiReturnsError_throwsBaselinkerApiException() {
        BaselinkerClient client = TestBaselinkerClientFactory.withStubbedResponse(
                request -> ERROR_TOKEN_BODY);
        OrdersService service = new OrdersService(client);

        BaselinkerApiException exception = assertThrows(BaselinkerApiException.class,
                () -> service.listRaw(GetOrdersRequest.builder().dateConfirmedFrom(INITIAL_CURSOR).build()));
        assertEquals("ERROR_EMPTY_TOKEN", exception.getErrorCode());
    }

    @Test
    void add_whenCalled_sendsMethodAndStatusIdParam() {
        AtomicReference<HttpRequest> captured = new AtomicReference<>();
        BaselinkerClient client = TestBaselinkerClientFactory.withStubbedResponses(
                captured, request -> addOrderSuccessBody());
        OrdersService service = new OrdersService(client);

        service.add(AddOrderRequest.builder()
                .orderStatusId(SAMPLE_ORDER_STATUS_ID)
                .dateAdd(INITIAL_CURSOR)
                .currency("PLN")
                .products(List.of(AddOrderProduct.builder()
                        .productId("12345")
                        .quantity(2)
                        .priceBrutto(new BigDecimal("10.00"))
                        .build()))
                .build());

        String body = extractBody(captured.get());
        assertTrue(body.startsWith(METHOD_PREFIX + "addOrder"),
                "body should start with addOrder method: " + body);
        String decodedParams = decodeParams(body);
        assertTrue(decodedParams.contains("\"order_status_id\":" + SAMPLE_ORDER_STATUS_ID),
                "params must contain order_status_id: " + decodedParams);
        assertTrue(decodedParams.contains("\"currency\":\"PLN\""),
                "params must contain currency: " + decodedParams);
    }

    @Test
    void add_whenApiReturnsSuccess_returnsParsedOrderId() {
        BaselinkerClient client = TestBaselinkerClientFactory.withStubbedResponse(
                request -> addOrderSuccessBody());
        OrdersService service = new OrdersService(client);

        AddOrderResponse response = service.add(AddOrderRequest.builder()
                .orderStatusId(SAMPLE_ORDER_STATUS_ID)
                .dateAdd(INITIAL_CURSOR)
                .currency("PLN")
                .products(List.of(AddOrderProduct.builder()
                        .productId("12345")
                        .quantity(1)
                        .priceBrutto(new BigDecimal("10.00"))
                        .build()))
                .build());

        assertNotNull(response);
        assertEquals(AddOrderResponse.StatusEnum.SUCCESS, response.getStatus());
        assertEquals((int) NEW_ORDER_ID, (int) response.getOrderId());
    }

    @Test
    void setStatus_whenCalled_sendsOrderIdAndStatusId() {
        AtomicReference<HttpRequest> captured = new AtomicReference<>();
        BaselinkerClient client = TestBaselinkerClientFactory.withStubbedResponses(
                captured, request -> "{\"status\":\"SUCCESS\"}");
        OrdersService service = new OrdersService(client);

        service.setStatus(ORDER_ID_FIRST, SAMPLE_ORDER_STATUS_ID);

        String decodedParams = decodeParams(extractBody(captured.get()));
        assertTrue(decodedParams.contains("\"order_id\":" + ORDER_ID_FIRST),
                "params must contain order_id: " + decodedParams);
        assertTrue(decodedParams.contains("\"status_id\":" + SAMPLE_ORDER_STATUS_ID),
                "params must contain status_id: " + decodedParams);
    }

    @Test
    void setStatus_whenApiReturnsError_throwsBaselinkerApiException() {
        BaselinkerClient client = TestBaselinkerClientFactory.withStubbedResponse(
                request -> ERROR_TOKEN_BODY);
        OrdersService service = new OrdersService(client);

        assertThrows(BaselinkerApiException.class,
                () -> service.setStatus(ORDER_ID_FIRST, SAMPLE_ORDER_STATUS_ID));
    }

    @Test
    void statusList_whenCalled_returnsParsedStatuses() {
        BaselinkerClient client = TestBaselinkerClientFactory.withStubbedResponse(
                request -> orderStatusListBody());
        OrdersService service = new OrdersService(client);

        List<OrderStatus> statuses = service.statusList();

        assertEquals(2, statuses.size());
        assertEquals(SAMPLE_ORDER_STATUS_ID, statuses.get(0).id());
        assertEquals("New orders", statuses.get(0).name());
    }

    @Test
    void statusList_whenApiReturnsEmptyStatuses_returnsEmptyList() {
        BaselinkerClient client = TestBaselinkerClientFactory.withStubbedResponse(
                request -> "{\"status\":\"SUCCESS\"}");
        OrdersService service = new OrdersService(client);

        List<OrderStatus> statuses = service.statusList();

        assertNotNull(statuses);
        assertEquals(List.of(), statuses);
    }

    @Test
    void constructor_whenClientIsNull_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new OrdersService(null));
    }

    @Test
    void getOrdersRequestBuilder_whenDateConfirmedFromMissing_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> GetOrdersRequest.builder().build());
    }

    @Test
    void getOrdersRequestToParams_whenIncludeUnconfirmedNotSet_omitsFromParams() {
        GetOrdersRequest request = GetOrdersRequest.builder()
                .dateConfirmedFrom(INITIAL_CURSOR)
                .build();

        assertNull(request.toParams(INITIAL_CURSOR).get("get_unconfirmed_orders"));
    }

    @Test
    void getOrdersRequestToParams_whenIncludeUnconfirmedSet_includesFlag() {
        GetOrdersRequest request = GetOrdersRequest.builder()
                .dateConfirmedFrom(INITIAL_CURSOR)
                .includeUnconfirmed(true)
                .build();

        assertEquals(Boolean.TRUE, request.toParams(INITIAL_CURSOR).get("get_unconfirmed_orders"));
    }

    @Test
    void addOrderRequestBuilder_whenProductsEmpty_throwsIllegalArgumentException() {
        AddOrderRequest.Builder builder = AddOrderRequest.builder()
                .orderStatusId(SAMPLE_ORDER_STATUS_ID)
                .dateAdd(INITIAL_CURSOR)
                .currency("PLN")
                .products(List.of());

        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    void addOrderProductBuilder_whenProductIdMissing_throwsNullPointerException() {
        AddOrderProduct.Builder builder = AddOrderProduct.builder()
                .quantity(1)
                .priceBrutto(new BigDecimal("1.00"));

        assertThrows(NullPointerException.class, builder::build);
    }

    private static String firstBatchBody() {
        long firstDate = INITIAL_CURSOR_EPOCH_SECONDS + FIRST_BATCH_MAX_DATE_OFFSET;
        return String.format("""
                {
                  "status": "SUCCESS",
                  "orders": [
                    {"order_id": %d, "date_confirmed": %d, "order_status_id": 1, "currency": "PLN"},
                    {"order_id": %d, "date_confirmed": %d, "order_status_id": 1, "currency": "PLN"}
                  ]
                }
                """,
                ORDER_ID_FIRST, firstDate - 5, ORDER_ID_SECOND, firstDate);
    }

    private static String secondBatchBody() {
        long secondCursorStart = INITIAL_CURSOR_EPOCH_SECONDS
                + FIRST_BATCH_MAX_DATE_OFFSET + CURSOR_INCREMENT_SECONDS;
        long thirdDate = INITIAL_CURSOR_EPOCH_SECONDS + SECOND_BATCH_DATE_OFFSET;
        return String.format("""
                {
                  "status": "SUCCESS",
                  "orders": [
                    {"order_id": %d, "date_confirmed": %d, "order_status_id": 2, "currency": "PLN"}
                  ],
                  "cursor_start": %d
                }
                """, ORDER_ID_THIRD, thirdDate, secondCursorStart);
    }

    private static String addOrderSuccessBody() {
        return String.format("{\"status\":\"SUCCESS\",\"order_id\":%d}", NEW_ORDER_ID);
    }

    private static String orderStatusListBody() {
        return String.format("""
                {
                  "status": "SUCCESS",
                  "statuses": [
                    {"id": %d, "name": "New orders", "name_for_customer": "Order accepted"},
                    {"id": %d, "name": "Shipped", "name_for_customer": "Shipped"}
                  ]
                }
                """, SAMPLE_ORDER_STATUS_ID, SAMPLE_ORDER_STATUS_ID + 1);
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
        String encoded = body.substring(index + PARAMETERS_PREFIX.length());
        return URLDecoder.decode(encoded, StandardCharsets.UTF_8);
    }
}
