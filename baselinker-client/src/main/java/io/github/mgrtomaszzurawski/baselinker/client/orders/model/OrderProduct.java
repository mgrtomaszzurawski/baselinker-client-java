package io.github.mgrtomaszzurawski.baselinker.client.orders.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * Line item of a BaseLinker order. Represents one product row inside an {@link Order}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderProduct(
        @JsonProperty("storage") String storage,
        @JsonProperty("storage_id") Long storageId,
        @JsonProperty("order_product_id") Long orderProductId,
        @JsonProperty("product_id") String productId,
        @JsonProperty("variant_id") String variantId,
        @JsonProperty("name") String name,
        @JsonProperty("sku") String sku,
        @JsonProperty("ean") String ean,
        @JsonProperty("location") String location,
        @JsonProperty("warehouse_id") Long warehouseId,
        @JsonProperty("attributes") String attributes,
        @JsonProperty("price_brutto") BigDecimal priceBrutto,
        @JsonProperty("tax_rate") BigDecimal taxRate,
        @JsonProperty("quantity") Integer quantity,
        @JsonProperty("weight") BigDecimal weight,
        @JsonProperty("bundle_id") Long bundleId
) {
}
