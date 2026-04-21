package io.github.mgrtomaszzurawski.baselinker.examples;

import io.github.mgrtomaszzurawski.baselinker.client.BaselinkerClient;
import io.github.mgrtomaszzurawski.baselinker.client.orders.GetOrdersRequest;
import io.github.mgrtomaszzurawski.baselinker.client.orders.model.Order;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Pull orders confirmed in the last 24 hours. The SDK auto-paginates using the
 * {@code date_confirmed_from + 1s} cursor convention — the caller only picks a
 * starting {@link Instant}.
 */
public final class SyncRecentOrders {

    private static final long LOOKBACK_HOURS = 24L;

    private SyncRecentOrders() {
    }

    public static void main(String[] args) {
        BaselinkerClient client = BaselinkerClient.builder()
                .apiToken(System.getenv("BASELINKER_TOKEN"))
                .build();

        Instant since = Instant.now().minus(LOOKBACK_HOURS, ChronoUnit.HOURS);

        client.orders()
                .list(GetOrdersRequest.builder()
                        .dateConfirmedFrom(since)
                        .build())
                .stream()
                .forEach(SyncRecentOrders::handleOrder);
    }

    private static void handleOrder(Order order) {
        System.out.printf("order %d status=%d email=%s%n",
                order.orderId(), order.orderStatusId(), order.email());
    }
}
