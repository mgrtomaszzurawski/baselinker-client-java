package io.github.mgrtomaszzurawski.baselinker.client.inventory.products;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.mgrtomaszzurawski.baselinker.client.inventory.model.InventoryProductLog;

import java.util.List;

/**
 * Wire response for {@code getInventoryProductLogs}. Page-paginated.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
record InventoryProductLogsResponse(
        @JsonProperty("status") String status,
        @JsonProperty("error_message") String errorMessage,
        @JsonProperty("error_code") String errorCode,
        @JsonProperty("logs") List<InventoryProductLog> logs
) {
    List<InventoryProductLog> logsOrEmpty() {
        return logs != null ? logs : List.of();
    }
}
