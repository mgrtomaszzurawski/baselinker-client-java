package io.github.mgrtomaszzurawski.baselinker.client.inventory.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Full product data from {@code getInventoryProductsData}. The v1 SDK exposes the
 * commonly used subset; additional wire fields (tags, text_fields, images, variants,
 * bundle_products, suppliers) are ignored during deserialization and can be retrieved
 * via the raw {@code client.execute(...)} escape hatch if needed.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record InventoryProductData(
        @JsonProperty("is_bundle") Boolean isBundle,
        @JsonProperty("parent_id") String parentId,
        @JsonProperty("sku") String sku,
        @JsonProperty("ean") String ean,
        @JsonProperty("tax_rate") BigDecimal taxRate,
        @JsonProperty("weight") BigDecimal weight,
        @JsonProperty("height") BigDecimal height,
        @JsonProperty("width") BigDecimal width,
        @JsonProperty("length") BigDecimal length,
        @JsonProperty("category_id") Long categoryId,
        @JsonProperty("manufacturer_id") Long manufacturerId,
        @JsonProperty("prices") Map<String, BigDecimal> prices,
        @JsonProperty("stock") Map<String, Integer> stock,
        @JsonProperty("average_cost") BigDecimal averageCost,
        @JsonProperty("average_landed_cost") BigDecimal averageLandedCost
) {
}
