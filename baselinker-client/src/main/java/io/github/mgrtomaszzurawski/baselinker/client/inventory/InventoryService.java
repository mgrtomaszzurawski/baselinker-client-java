package io.github.mgrtomaszzurawski.baselinker.client.inventory;

import io.github.mgrtomaszzurawski.baselinker.client.BaselinkerClient;
import io.github.mgrtomaszzurawski.baselinker.client.BaselinkerException;
import io.github.mgrtomaszzurawski.baselinker.client.inventory.model.Inventory;
import io.github.mgrtomaszzurawski.baselinker.client.inventory.model.InventoryWarehouse;
import io.github.mgrtomaszzurawski.baselinker.client.inventory.products.InventoryProductsService;

import java.util.List;
import java.util.Objects;

/**
 * Typed access to BaseLinker inventory methods. Obtain via
 * {@link BaselinkerClient#inventory()}. Also exposes {@link #products()} for the
 * product-level sub-service.
 */
public final class InventoryService {

    private static final String METHOD_GET_INVENTORIES = "getInventories";
    private static final String METHOD_GET_INVENTORY_WAREHOUSES = "getInventoryWarehouses";

    private final BaselinkerClient client;
    private final InventoryProductsService products;

    public InventoryService(BaselinkerClient client) {
        this.client = Objects.requireNonNull(client, "client must not be null");
        this.products = new InventoryProductsService(client);
    }

    /**
     * Returns the catalogs (inventories) configured on the BaseLinker account.
     * Cache at startup — these rarely change and provide the {@code inventoryId}
     * required by most product-level calls.
     */
    public List<Inventory> catalogs() throws BaselinkerException {
        InventoriesResponse response = client.execute(
                METHOD_GET_INVENTORIES, InventoriesResponse.class);
        return response.inventoriesOrEmpty();
    }

    /**
     * Returns the warehouses configured on the BaseLinker account.
     * Cache at startup.
     */
    public List<InventoryWarehouse> warehouses() throws BaselinkerException {
        InventoryWarehousesResponse response = client.execute(
                METHOD_GET_INVENTORY_WAREHOUSES, InventoryWarehousesResponse.class);
        return response.warehousesOrEmpty();
    }

    /**
     * Product-level sub-service (list, data, stock, updateStock, logs).
     */
    public InventoryProductsService products() {
        return products;
    }
}
