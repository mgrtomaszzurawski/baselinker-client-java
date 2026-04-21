package io.github.mgrtomaszzurawski.baselinker.client.documents;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Wire response for {@code setInventoryDocumentStatusConfirmed}. Package-private.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
record DocumentStatusOnlyResponse(
        @JsonProperty("status") String status,
        @JsonProperty("error_message") String errorMessage,
        @JsonProperty("error_code") String errorCode
) {
}
