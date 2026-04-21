package io.github.mgrtomaszzurawski.baselinker.client.inventory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.mgrtomaszzurawski.baselinker.client.inventory.model.InventoryWarehouse;

import java.util.List;

/**
 * Wire response for {@code getInventoryWarehouses}. Package-private.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
record InventoryWarehousesResponse(
        @JsonProperty("status") String status,
        @JsonProperty("error_message") String errorMessage,
        @JsonProperty("error_code") String errorCode,
        @JsonProperty("warehouses") List<InventoryWarehouse> warehouses
) {
    List<InventoryWarehouse> warehousesOrEmpty() {
        return warehouses != null ? warehouses : List.of();
    }
}
