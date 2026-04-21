package io.github.mgrtomaszzurawski.baselinker.client.orders;

import io.github.mgrtomaszzurawski.baselinker.client.BaselinkerClient;
import io.github.mgrtomaszzurawski.baselinker.client.BaselinkerException;
import io.github.mgrtomaszzurawski.baselinker.client.model.AddOrderResponse;
import io.github.mgrtomaszzurawski.baselinker.client.orders.model.Order;
import io.github.mgrtomaszzurawski.baselinker.client.orders.model.OrderStatus;
import io.github.mgrtomaszzurawski.baselinker.client.pagination.DateCursorResultPage;
import io.github.mgrtomaszzurawski.baselinker.client.pagination.ResultPage;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Typed access to BaseLinker order methods. Obtain via {@link BaselinkerClient#orders()}.
 *
 * <p>Wraps the following API methods: {@code getOrders}, {@code addOrder},
 * {@code setOrderStatus}, {@code getOrderStatusList}.
 */
public final class OrdersService {

    private static final String METHOD_GET_ORDERS = "getOrders";
    private static final String METHOD_ADD_ORDER = "addOrder";
    private static final String METHOD_SET_ORDER_STATUS = "setOrderStatus";
    private static final String METHOD_GET_ORDER_STATUS_LIST = "getOrderStatusList";
    private static final String PARAM_ORDER_ID = "order_id";
    private static final String PARAM_STATUS_ID = "status_id";

    private final BaselinkerClient client;

    public OrdersService(BaselinkerClient client) {
        this.client = Objects.requireNonNull(client, "client must not be null");
    }

    /**
     * Streams all orders confirmed on or after {@code request.dateConfirmedFrom()},
     * auto-paginating using the BaseLinker time cursor contract. The returned
     * {@link ResultPage} is single-use.
     */
    public ResultPage<Order> list(GetOrdersRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new DateCursorResultPage<>(
                cursor -> fetchOrdersBatch(request, cursor),
                order -> order.dateConfirmed() != null ? order.dateConfirmed().getEpochSecond() : 0L,
                request.dateConfirmedFrom()
        );
    }

    /**
     * Returns a single batch of orders (at most ~100 items) without pagination.
     * Prefer {@link #list(GetOrdersRequest)} for anything multi-page.
     */
    public List<Order> listRaw(GetOrdersRequest request) throws BaselinkerException {
        Objects.requireNonNull(request, "request must not be null");
        return fetchOrdersBatch(request, request.dateConfirmedFrom());
    }

    /**
     * Creates a new BaseLinker order. Returns the generated {@link AddOrderResponse} whose
     * {@code getOrderId()} holds the new id.
     */
    public AddOrderResponse add(AddOrderRequest request) throws BaselinkerException {
        Objects.requireNonNull(request, "request must not be null");
        return client.execute(METHOD_ADD_ORDER, request.toParams(), AddOrderResponse.class);
    }

    /**
     * Moves an order to a new status. Throws {@code BaselinkerApiException} if the
     * transition is rejected by BaseLinker.
     */
    public void setStatus(long orderId, long statusId) throws BaselinkerException {
        Map<String, Object> params = Map.of(
                PARAM_ORDER_ID, orderId,
                PARAM_STATUS_ID, statusId
        );
        client.execute(METHOD_SET_ORDER_STATUS, params, StatusOnlyResponse.class);
    }

    /**
     * Returns the order status list configured on the BaseLinker account. Cache at
     * startup — this rarely changes and is required to resolve status IDs.
     */
    public List<OrderStatus> statusList() throws BaselinkerException {
        OrderStatusListResponse response = client.execute(
                METHOD_GET_ORDER_STATUS_LIST, OrderStatusListResponse.class);
        return response.statusesOrEmpty();
    }

    private List<Order> fetchOrdersBatch(GetOrdersRequest request, Instant cursor) {
        OrdersBatchResponse response = client.execute(
                METHOD_GET_ORDERS, request.toParams(cursor), OrdersBatchResponse.class);
        return response.ordersOrEmpty();
    }
}
