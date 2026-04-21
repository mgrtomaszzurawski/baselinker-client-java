package io.github.mgrtomaszzurawski.baselinker.examples;

import io.github.mgrtomaszzurawski.baselinker.client.BaselinkerClient;
import io.github.mgrtomaszzurawski.baselinker.client.inventory.products.UpdateInventoryProductsStockRequest;
import io.github.mgrtomaszzurawski.baselinker.client.inventory.products.UpdateInventoryProductsStockResult;

import java.util.Map;

/**
 * Update stock levels for two products across two warehouses in one call.
 * {@code bl_<warehouse_id>} is BaseLinker's wire format for warehouse keys.
 */
public final class UpdateStock {

    private static final long INVENTORY_ID = 307L;
    private static final String PRODUCT_A = "2685";
    private static final String PRODUCT_B = "2687";
    private static final String WAREHOUSE_A = "bl_206";
    private static final String WAREHOUSE_B = "bl_207";

    private UpdateStock() {
    }

    public static void main(String[] args) {
        BaselinkerClient client = BaselinkerClient.builder()
                .apiToken(System.getenv("BASELINKER_TOKEN"))
                .build();

        UpdateInventoryProductsStockResult result = client.inventory()
                .products()
                .updateStock(UpdateInventoryProductsStockRequest.of(INVENTORY_ID, Map.of(
                        PRODUCT_A, Map.of(WAREHOUSE_A, 5, WAREHOUSE_B, 7),
                        PRODUCT_B, Map.of(WAREHOUSE_A, 2, WAREHOUSE_B, 4)
                )));

        System.out.println("Updated products: " + result.counter());
        if (!result.warningsOrEmpty().isEmpty()) {
            System.out.println("Warnings: " + result.warningsOrEmpty());
        }
    }
}
