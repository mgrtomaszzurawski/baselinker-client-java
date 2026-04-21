package io.github.mgrtomaszzurawski.baselinker.client.orders;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.mgrtomaszzurawski.baselinker.client.orders.model.Order;

import java.util.List;

/**
 * Wire response for {@code getOrders}. Package-private: exposed externally only as an
 * {@link Order} list via {@link OrdersService#listRaw(GetOrdersRequest)}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
record OrdersBatchResponse(
        @JsonProperty("status") String status,
        @JsonProperty("error_message") String errorMessage,
        @JsonProperty("error_code") String errorCode,
        @JsonProperty("orders") List<Order> orders
) {
    List<Order> ordersOrEmpty() {
        return orders != null ? orders : List.of();
    }
}
