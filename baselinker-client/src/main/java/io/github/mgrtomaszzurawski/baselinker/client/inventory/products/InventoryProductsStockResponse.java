package io.github.mgrtomaszzurawski.baselinker.client.inventory.products;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.mgrtomaszzurawski.baselinker.client.inventory.model.InventoryStockEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Wire response for {@code getInventoryProductsStock}. Products come keyed by id;
 * the SDK flattens to a list.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
record InventoryProductsStockResponse(
        @JsonProperty("status") String status,
        @JsonProperty("error_message") String errorMessage,
        @JsonProperty("error_code") String errorCode,
        @JsonProperty("products") Map<String, InventoryStockEntry> products
) {
    List<InventoryStockEntry> productsAsList() {
        if (products == null) {
            return List.of();
        }
        return new ArrayList<>(products.values());
    }
}
