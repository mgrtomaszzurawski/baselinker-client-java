package io.github.mgrtomaszzurawski.baselinker.client.inventory;

import io.github.mgrtomaszzurawski.baselinker.client.BaselinkerApiException;
import io.github.mgrtomaszzurawski.baselinker.client.BaselinkerClient;
import io.github.mgrtomaszzurawski.baselinker.client.TestBaselinkerClientFactory;
import io.github.mgrtomaszzurawski.baselinker.client.inventory.model.Inventory;
import io.github.mgrtomaszzurawski.baselinker.client.inventory.model.InventoryWarehouse;
import io.github.mgrtomaszzurawski.baselinker.client.inventory.model.WarehouseType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InventoryServiceTest {

    private static final long FIRST_INVENTORY_ID = 307L;
    private static final long SECOND_INVENTORY_ID = 401L;
    private static final long DEFAULT_WAREHOUSE_ID = 205L;
    private static final long SHOP_WAREHOUSE_ID = 2334L;
    private static final String ERROR_BODY =
            "{\"status\":\"ERROR\",\"error_code\":\"ERROR_BAD\",\"error_message\":\"bad\"}";

    @Test
    void catalogs_whenCalled_returnsParsedInventories() {
        BaselinkerClient client = TestBaselinkerClientFactory.withStubbedResponse(
                request -> inventoriesBody());
        InventoryService service = new InventoryService(client);

        List<Inventory> catalogs = service.catalogs();

        assertEquals(2, catalogs.size());
        assertEquals(FIRST_INVENTORY_ID, catalogs.get(0).inventoryId());
        assertEquals("Main", catalogs.get(0).name());
        assertEquals(SECOND_INVENTORY_ID, catalogs.get(1).inventoryId());
    }

    @Test
    void catalogs_whenApiReturnsError_throwsBaselinkerApiException() {
        BaselinkerClient client = TestBaselinkerClientFactory.withStubbedResponse(
                request -> ERROR_BODY);
        InventoryService service = new InventoryService(client);

        assertThrows(BaselinkerApiException.class, service::catalogs);
    }

    @Test
    void warehouses_whenCalled_returnsParsedWarehousesWithTypedEnum() {
        BaselinkerClient client = TestBaselinkerClientFactory.withStubbedResponse(
                request -> warehousesBody());
        InventoryService service = new InventoryService(client);

        List<InventoryWarehouse> warehouses = service.warehouses();

        assertEquals(2, warehouses.size());
        assertEquals(WarehouseType.BASELINKER, warehouses.get(0).warehouseType());
        assertEquals(DEFAULT_WAREHOUSE_ID, warehouses.get(0).warehouseId());
        assertEquals(WarehouseType.SHOP, warehouses.get(1).warehouseType());
        assertEquals(SHOP_WAREHOUSE_ID, warehouses.get(1).warehouseId());
    }

    @Test
    void warehouses_whenApiReturnsEmptyWarehouses_returnsEmptyList() {
        BaselinkerClient client = TestBaselinkerClientFactory.withStubbedResponse(
                request -> "{\"status\":\"SUCCESS\"}");
        InventoryService service = new InventoryService(client);

        List<InventoryWarehouse> warehouses = service.warehouses();

        assertEquals(List.of(), warehouses);
    }

    @Test
    void products_whenCalled_returnsSubService() {
        BaselinkerClient client = TestBaselinkerClientFactory.withStubbedResponse(
                request -> "{\"status\":\"SUCCESS\"}");
        InventoryService service = new InventoryService(client);

        assertNotNull(service.products(), "products sub-service must not be null");
    }

    @Test
    void constructor_whenClientIsNull_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new InventoryService(null));
    }

    private static String inventoriesBody() {
        return String.format("""
                {
                  "status": "SUCCESS",
                  "inventories": [
                    {"inventory_id": %d, "name": "Main", "default_language": "en", "is_default": true},
                    {"inventory_id": %d, "name": "Secondary", "default_language": "en", "is_default": false}
                  ]
                }
                """, FIRST_INVENTORY_ID, SECOND_INVENTORY_ID);
    }

    private static String warehousesBody() {
        return String.format("""
                {
                  "status": "SUCCESS",
                  "warehouses": [
                    {"warehouse_type": "bl", "warehouse_id": %d, "name": "Default", "stock_edition": true, "is_default": true},
                    {"warehouse_type": "shop", "warehouse_id": %d, "name": "MyShop.com", "stock_edition": false, "is_default": false}
                  ]
                }
                """, DEFAULT_WAREHOUSE_ID, SHOP_WAREHOUSE_ID);
    }
}
