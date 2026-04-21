package io.github.mgrtomaszzurawski.baselinker.client.documents;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.mgrtomaszzurawski.baselinker.client.documents.model.InventoryDocumentSeries;

import java.util.List;

/**
 * Wire response for {@code getInventoryDocumentSeries}. Package-private.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
record InventoryDocumentSeriesResponse(
        @JsonProperty("status") String status,
        @JsonProperty("error_message") String errorMessage,
        @JsonProperty("error_code") String errorCode,
        @JsonProperty("document_series") List<InventoryDocumentSeries> documentSeries
) {
    List<InventoryDocumentSeries> documentSeriesOrEmpty() {
        return documentSeries != null ? documentSeries : List.of();
    }
}
