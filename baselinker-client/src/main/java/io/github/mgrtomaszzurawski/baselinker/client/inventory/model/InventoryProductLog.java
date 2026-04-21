package io.github.mgrtomaszzurawski.baselinker.client.inventory.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

/**
 * One product log event from {@code getInventoryProductLogs}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record InventoryProductLog(
        @JsonProperty("profile") String profile,
        @JsonProperty("date") Instant date,
        @JsonProperty("entries") List<LogEntry> entries
) {

    /**
     * One entry inside an {@link InventoryProductLog}. The meaning of
     * {@code from}/{@code to}/{@code info} varies by {@link #type()} per
     * {@code docs/methods/getInventoryProductLogs.md}.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record LogEntry(
            @JsonProperty("type") Integer type,
            @JsonProperty("from") String from,
            @JsonProperty("to") String to,
            @JsonProperty("info") String info
    ) {
    }
}
