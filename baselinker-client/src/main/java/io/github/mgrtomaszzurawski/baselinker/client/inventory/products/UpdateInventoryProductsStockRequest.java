package io.github.mgrtomaszzurawski.baselinker.client.inventory.products;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Typed request for {@code updateInventoryProductsStock}. Maximum 1000 products per call
 * per the BaseLinker API contract — the SDK does not enforce this; the server returns an
 * error if violated.
 *
 * <p>{@code stockByProduct} maps {@code productId} (or {@code variantId}) to a map of
 * {@code warehouseKey → qty}, where {@code warehouseKey} is the BaseLinker wire format
 * (for example {@code bl_206}).
 */
public record UpdateInventoryProductsStockRequest(
        long inventoryId,
        Map<String, Map<String, Integer>> stockByProduct
) {

    private static final String PARAM_INVENTORY_ID = "inventory_id";
    private static final String PARAM_PRODUCTS = "products";

    public UpdateInventoryProductsStockRequest {
        Objects.requireNonNull(stockByProduct, "stockByProduct must not be null");
        if (stockByProduct.isEmpty()) {
            throw new IllegalArgumentException("stockByProduct must not be empty");
        }
        stockByProduct = Map.copyOf(stockByProduct);
    }

    public static UpdateInventoryProductsStockRequest of(long inventoryId,
                                                         Map<String, Map<String, Integer>> stockByProduct) {
        return new UpdateInventoryProductsStockRequest(inventoryId, stockByProduct);
    }

    public Map<String, Object> toParams() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put(PARAM_INVENTORY_ID, inventoryId);
        params.put(PARAM_PRODUCTS, stockByProduct);
        return params;
    }
}
