package io.github.mgrtomaszzurawski.baselinker.client.inventory.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

/**
 * Type of an {@link InventoryWarehouse}. BaseLinker uses three values across the API:
 * {@code bl} for native warehouses, {@code shop} and {@code warehouse} for integrations.
 */
public enum WarehouseType {

    BASELINKER("bl"),
    SHOP("shop"),
    WAREHOUSE("warehouse");

    private final String wireValue;

    WarehouseType(String wireValue) {
        this.wireValue = wireValue;
    }

    @JsonValue
    public String wireValue() {
        return wireValue;
    }

    @JsonCreator
    public static WarehouseType fromWireValue(String value) {
        return Arrays.stream(values())
                .filter(type -> type.wireValue.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown warehouse type: " + value));
    }
}
