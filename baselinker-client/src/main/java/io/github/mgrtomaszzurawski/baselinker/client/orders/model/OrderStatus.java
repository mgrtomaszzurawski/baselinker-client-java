package io.github.mgrtomaszzurawski.baselinker.client.orders.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * One entry from the order status list configured per BaseLinker account
 * (as returned by {@code getOrderStatusList}).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderStatus(
        @JsonProperty("id") long id,
        @JsonProperty("name") String name,
        @JsonProperty("name_for_customer") String nameForCustomer,
        @JsonProperty("color") String color
) {
}
