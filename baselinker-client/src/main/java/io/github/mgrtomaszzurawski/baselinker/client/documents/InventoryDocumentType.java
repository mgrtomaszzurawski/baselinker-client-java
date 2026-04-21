package io.github.mgrtomaszzurawski.baselinker.client.documents;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

/**
 * Type of an inventory document. Wire values 0..5 per
 * {@code docs/methods/addInventoryDocument.md}.
 */
public enum InventoryDocumentType {

    GOODS_RECEIPT(0),
    INTERNAL_GOODS_RECEIPT(1),
    GOODS_ISSUE(2),
    INTERNAL_GOODS_ISSUE(3),
    INTERNAL_TRANSFER(4),
    OTHER(5);

    private final int wireValue;

    InventoryDocumentType(int wireValue) {
        this.wireValue = wireValue;
    }

    @JsonValue
    public int wireValue() {
        return wireValue;
    }

    @JsonCreator
    public static InventoryDocumentType fromWireValue(int value) {
        return Arrays.stream(values())
                .filter(type -> type.wireValue == value)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown inventory document type: " + value));
    }
}
