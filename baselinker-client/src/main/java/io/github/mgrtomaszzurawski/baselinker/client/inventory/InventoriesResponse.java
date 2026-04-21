package io.github.mgrtomaszzurawski.baselinker.client.inventory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.mgrtomaszzurawski.baselinker.client.inventory.model.Inventory;

import java.util.List;

/**
 * Wire response for {@code getInventories}. Package-private.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
record InventoriesResponse(
        @JsonProperty("status") String status,
        @JsonProperty("error_message") String errorMessage,
        @JsonProperty("error_code") String errorCode,
        @JsonProperty("inventories") List<Inventory> inventories
) {
    List<Inventory> inventoriesOrEmpty() {
        return inventories != null ? inventories : List.of();
    }
}
