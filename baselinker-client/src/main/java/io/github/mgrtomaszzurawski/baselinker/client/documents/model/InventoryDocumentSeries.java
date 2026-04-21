package io.github.mgrtomaszzurawski.baselinker.client.documents.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.mgrtomaszzurawski.baselinker.client.documents.InventoryDocumentType;

/**
 * One entry from {@code getInventoryDocumentSeries} — a document numbering series
 * associated with a warehouse.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record InventoryDocumentSeries(
        @JsonProperty("document_series_id") long documentSeriesId,
        @JsonProperty("name") String name,
        @JsonProperty("type") InventoryDocumentType type,
        @JsonProperty("warehouse_id") long warehouseId,
        @JsonProperty("format") String format
) {
}
