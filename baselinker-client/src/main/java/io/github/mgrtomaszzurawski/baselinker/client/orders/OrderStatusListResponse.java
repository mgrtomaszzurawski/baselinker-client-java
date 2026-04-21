package io.github.mgrtomaszzurawski.baselinker.client.orders;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.mgrtomaszzurawski.baselinker.client.orders.model.OrderStatus;

import java.util.List;

/**
 * Wire response for {@code getOrderStatusList}. Package-private.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
record OrderStatusListResponse(
        @JsonProperty("status") String status,
        @JsonProperty("error_message") String errorMessage,
        @JsonProperty("error_code") String errorCode,
        @JsonProperty("statuses") List<OrderStatus> statuses
) {
    List<OrderStatus> statusesOrEmpty() {
        return statuses != null ? statuses : List.of();
    }
}
