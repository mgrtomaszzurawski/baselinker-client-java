package io.github.mgrtomaszzurawski.baselinker.client.inventory.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * One entry from {@code getInventoryWarehouses}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record InventoryWarehouse(
        @JsonProperty("warehouse_type") WarehouseType warehouseType,
        @JsonProperty("warehouse_id") long warehouseId,
        @JsonProperty("name") String name,
        @JsonProperty("description") String description,
        @JsonProperty("stock_edition") Boolean stockEdition,
        @JsonProperty("is_default") Boolean isDefault
) {
}
