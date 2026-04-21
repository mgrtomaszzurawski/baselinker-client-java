package io.github.mgrtomaszzurawski.baselinker.client.inventory.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Basic product entry from {@code getInventoryProductsList}. For full fields use
 * {@code getInventoryProductsData} and the typed {@link InventoryProductData}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record InventoryProduct(
        @JsonProperty("id") String id,
        @JsonProperty("parent_id") String parentId,
        @JsonProperty("ean") String ean,
        @JsonProperty("sku") String sku,
        @JsonProperty("name") String name,
        @JsonProperty("prices") Map<String, BigDecimal> prices,
        @JsonProperty("stock") Map<String, Integer> stock
) {
}
