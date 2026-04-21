package io.github.mgrtomaszzurawski.baselinker.client.orders;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * One product line for {@link AddOrderRequest}. Maps to BaseLinker's nested
 * {@code products[]} array in the {@code addOrder} wire format.
 */
public record AddOrderProduct(
        String storage,
        Long storageId,
        String productId,
        String variantId,
        String name,
        String sku,
        String ean,
        Long warehouseId,
        String attributes,
        BigDecimal priceBrutto,
        BigDecimal taxRate,
        Integer quantity,
        BigDecimal weight
) {

    private static final String PARAM_STORAGE = "storage";
    private static final String PARAM_STORAGE_ID = "storage_id";
    private static final String PARAM_PRODUCT_ID = "product_id";
    private static final String PARAM_VARIANT_ID = "variant_id";
    private static final String PARAM_NAME = "name";
    private static final String PARAM_SKU = "sku";
    private static final String PARAM_EAN = "ean";
    private static final String PARAM_WAREHOUSE_ID = "warehouse_id";
    private static final String PARAM_ATTRIBUTES = "attributes";
    private static final String PARAM_PRICE_BRUTTO = "price_brutto";
    private static final String PARAM_TAX_RATE = "tax_rate";
    private static final String PARAM_QUANTITY = "quantity";
    private static final String PARAM_WEIGHT = "weight";

    public AddOrderProduct {
        Objects.requireNonNull(productId, "productId must not be null");
        Objects.requireNonNull(quantity, "quantity must not be null");
        Objects.requireNonNull(priceBrutto, "priceBrutto must not be null");
    }

    public static Builder builder() {
        return new Builder();
    }

    public Map<String, Object> toParams() {
        Map<String, Object> params = new LinkedHashMap<>();
        putIfNotNull(params, PARAM_STORAGE, storage);
        putIfNotNull(params, PARAM_STORAGE_ID, storageId);
        params.put(PARAM_PRODUCT_ID, productId);
        putIfNotNull(params, PARAM_VARIANT_ID, variantId);
        putIfNotNull(params, PARAM_NAME, name);
        putIfNotNull(params, PARAM_SKU, sku);
        putIfNotNull(params, PARAM_EAN, ean);
        putIfNotNull(params, PARAM_WAREHOUSE_ID, warehouseId);
        putIfNotNull(params, PARAM_ATTRIBUTES, attributes);
        params.put(PARAM_PRICE_BRUTTO, priceBrutto);
        putIfNotNull(params, PARAM_TAX_RATE, taxRate);
        params.put(PARAM_QUANTITY, quantity);
        putIfNotNull(params, PARAM_WEIGHT, weight);
        return params;
    }

    private static void putIfNotNull(Map<String, Object> target, String key, Object value) {
        if (value != null) {
            target.put(key, value);
        }
    }

    public static final class Builder {
        private String storage;
        private Long storageId;
        private String productId;
        private String variantId;
        private String name;
        private String sku;
        private String ean;
        private Long warehouseId;
        private String attributes;
        private BigDecimal priceBrutto;
        private BigDecimal taxRate;
        private Integer quantity;
        private BigDecimal weight;

        private Builder() {
        }

        public Builder storage(String storage) {
            this.storage = storage;
            return this;
        }

        public Builder storageId(long storageId) {
            this.storageId = storageId;
            return this;
        }

        public Builder productId(String productId) {
            this.productId = productId;
            return this;
        }

        public Builder variantId(String variantId) {
            this.variantId = variantId;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder sku(String sku) {
            this.sku = sku;
            return this;
        }

        public Builder ean(String ean) {
            this.ean = ean;
            return this;
        }

        public Builder warehouseId(long warehouseId) {
            this.warehouseId = warehouseId;
            return this;
        }

        public Builder attributes(String attributes) {
            this.attributes = attributes;
            return this;
        }

        public Builder priceBrutto(BigDecimal priceBrutto) {
            this.priceBrutto = priceBrutto;
            return this;
        }

        public Builder taxRate(BigDecimal taxRate) {
            this.taxRate = taxRate;
            return this;
        }

        public Builder quantity(int quantity) {
            this.quantity = quantity;
            return this;
        }

        public Builder weight(BigDecimal weight) {
            this.weight = weight;
            return this;
        }

        public AddOrderProduct build() {
            return new AddOrderProduct(storage, storageId, productId, variantId, name, sku, ean,
                    warehouseId, attributes, priceBrutto, taxRate, quantity, weight);
        }
    }
}
