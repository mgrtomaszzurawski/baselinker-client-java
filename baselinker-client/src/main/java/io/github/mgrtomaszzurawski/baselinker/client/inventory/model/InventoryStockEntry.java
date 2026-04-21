package io.github.mgrtomaszzurawski.baselinker.client.inventory.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Stock snapshot for a single product as returned by {@code getInventoryProductsStock}.
 * The outer response keys this by product id; the SDK flattens to a list of entries.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record InventoryStockEntry(
        @JsonProperty("product_id") String productId,
        @JsonProperty("stock") Map<String, Integer> stockByWarehouse,
        @JsonProperty("reservations") Map<String, Integer> reservationsByWarehouse,
        @JsonProperty("variants") Map<String, Map<String, Integer>> variants
) {
}
