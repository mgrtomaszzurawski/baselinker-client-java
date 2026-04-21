package io.github.mgrtomaszzurawski.baselinker.examples;

import io.github.mgrtomaszzurawski.baselinker.client.BaselinkerClient;
import io.github.mgrtomaszzurawski.baselinker.client.model.AddOrderResponse;
import io.github.mgrtomaszzurawski.baselinker.client.orders.AddOrderProduct;
import io.github.mgrtomaszzurawski.baselinker.client.orders.AddOrderRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Create a BaseLinker order with a single product line. Typical use: modeling an
 * internal transfer or a pickup-from-store flow as an order record.
 */
public final class CreateOrder {

    private static final long DEMO_STATUS_ID = 1051L;
    private static final int DEMO_QUANTITY = 3;
    private static final BigDecimal DEMO_PRICE = new BigDecimal("0.00");

    private CreateOrder() {
    }

    public static void main(String[] args) {
        BaselinkerClient client = BaselinkerClient.builder()
                .apiToken(System.getenv("BASELINKER_TOKEN"))
                .build();

        AddOrderResponse created = client.orders().add(AddOrderRequest.builder()
                .orderStatusId(DEMO_STATUS_ID)
                .dateAdd(Instant.now())
                .currency("PLN")
                .userComments("Pickup — Warsaw")
                .products(List.of(AddOrderProduct.builder()
                        .storage("db")
                        .productId("12345")
                        .quantity(DEMO_QUANTITY)
                        .priceBrutto(DEMO_PRICE)
                        .build()))
                .build());

        System.out.println("Created order id = " + created.getOrderId());
    }
}
