package io.github.mgrtomaszzurawski.baselinker.client.inventory.products;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.mgrtomaszzurawski.baselinker.client.inventory.model.InventoryProductData;

import java.util.Map;

/**
 * Wire response for {@code getInventoryProductsData}. Products come keyed by id.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
record InventoryProductsDataResponse(
        @JsonProperty("status") String status,
        @JsonProperty("error_message") String errorMessage,
        @JsonProperty("error_code") String errorCode,
        @JsonProperty("products") Map<String, InventoryProductData> products
) {
    Map<String, InventoryProductData> productsOrEmpty() {
        return products != null ? products : Map.of();
    }
}
