package io.github.mgrtomaszzurawski.baselinker.client.documents;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Wire response for {@code addInventoryDocumentItems}. {@code items} lists the created
 * items (each currently exposes only {@code item_id} in the BaseLinker API).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AddInventoryDocumentItemsResult(
        @JsonProperty("status") String status,
        @JsonProperty("error_message") String errorMessage,
        @JsonProperty("error_code") String errorCode,
        @JsonProperty("items") List<CreatedItem> items
) {

    public List<CreatedItem> itemsOrEmpty() {
        return items != null ? items : List.of();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CreatedItem(@JsonProperty("item_id") long itemId) {
    }
}
