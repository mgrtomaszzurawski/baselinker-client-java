package io.github.mgrtomaszzurawski.baselinker.client.inventory.products;

import java.util.Arrays;

/**
 * Type of log event returned by {@code getInventoryProductLogs}.
 * Wire values are integers 1..10 per {@code docs/methods/getInventoryProductLogs.md}.
 */
public enum InventoryLogType {

    STOCK(1),
    PRICE(2),
    CREATION(3),
    DELETION(4),
    TEXT(5),
    LOCATIONS(6),
    LINKS(7),
    GALLERY(8),
    VARIANTS(9),
    BUNDLE(10);

    private final int wireValue;

    InventoryLogType(int wireValue) {
        this.wireValue = wireValue;
    }

    public int wireValue() {
        return wireValue;
    }

    public static InventoryLogType fromWireValue(int value) {
        return Arrays.stream(values())
                .filter(type -> type.wireValue == value)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown inventory log type: " + value));
    }
}
