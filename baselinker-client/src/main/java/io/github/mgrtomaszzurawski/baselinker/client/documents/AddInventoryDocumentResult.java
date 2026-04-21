package io.github.mgrtomaszzurawski.baselinker.client.documents;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Wire response for {@code addInventoryDocument}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AddInventoryDocumentResult(
        @JsonProperty("status") String status,
        @JsonProperty("error_message") String errorMessage,
        @JsonProperty("error_code") String errorCode,
        @JsonProperty("document_id") long documentId,
        @JsonProperty("document_number") String documentNumber
) {
}
