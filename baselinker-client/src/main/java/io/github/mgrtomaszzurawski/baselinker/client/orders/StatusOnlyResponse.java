package io.github.mgrtomaszzurawski.baselinker.client.orders;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Wire response for BaseLinker methods that return only the status envelope
 * (e.g. {@code setOrderStatus}). Package-private.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
record StatusOnlyResponse(
        @JsonProperty("status") String status,
        @JsonProperty("error_message") String errorMessage,
        @JsonProperty("error_code") String errorCode
) {
}
