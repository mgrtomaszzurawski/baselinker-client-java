package io.github.mgrtomaszzurawski.baselinker.client.inventory.products;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.mgrtomaszzurawski.baselinker.client.inventory.model.InventoryProduct;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Wire response for {@code getInventoryProductsList}. Products come keyed by id;
 * the SDK flattens to an ordered list.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
record InventoryProductsListResponse(
        @JsonProperty("status") String status,
        @JsonProperty("error_message") String errorMessage,
        @JsonProperty("error_code") String errorCode,
        @JsonProperty("products") Map<String, InventoryProduct> products
) {
    List<InventoryProduct> productsAsList() {
        if (products == null) {
            return List.of();
        }
        return new ArrayList<>(products.values());
    }
}
