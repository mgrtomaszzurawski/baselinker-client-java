package io.github.mgrtomaszzurawski.baselinker.client.inventory.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * One entry from {@code getInventories}: a BaseLinker catalog.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Inventory(
        @JsonProperty("inventory_id") long inventoryId,
        @JsonProperty("name") String name,
        @JsonProperty("description") String description,
        @JsonProperty("languages") List<String> languages,
        @JsonProperty("default_language") String defaultLanguage,
        @JsonProperty("price_groups") List<Long> priceGroups,
        @JsonProperty("default_price_group") Long defaultPriceGroup,
        @JsonProperty("warehouses") List<String> warehouses,
        @JsonProperty("default_warehouse") String defaultWarehouse,
        @JsonProperty("reservations") Boolean reservations,
        @JsonProperty("is_default") Boolean isDefault
) {
}
