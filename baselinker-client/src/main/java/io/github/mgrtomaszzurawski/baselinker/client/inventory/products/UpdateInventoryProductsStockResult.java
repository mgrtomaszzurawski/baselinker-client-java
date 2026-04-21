package io.github.mgrtomaszzurawski.baselinker.client.inventory.products;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Wire response for {@code updateInventoryProductsStock}. {@code counter} is the number
 * of products successfully updated; {@code warnings} maps product IDs to error messages
 * for the subset that failed.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record UpdateInventoryProductsStockResult(
        @JsonProperty("status") String status,
        @JsonProperty("error_message") String errorMessage,
        @JsonProperty("error_code") String errorCode,
        @JsonProperty("counter") Integer counter,
        @JsonProperty("warnings") Map<String, String> warnings
) {
    public Map<String, String> warningsOrEmpty() {
        return warnings != null ? warnings : Map.of();
    }
}
