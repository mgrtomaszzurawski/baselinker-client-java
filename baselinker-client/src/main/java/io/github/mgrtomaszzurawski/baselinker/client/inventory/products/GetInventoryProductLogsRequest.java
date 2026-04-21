package io.github.mgrtomaszzurawski.baselinker.client.inventory.products;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Typed request for {@code getInventoryProductLogs}. {@code productId} is required.
 */
public record GetInventoryProductLogsRequest(
        String productId,
        Instant dateFrom,
        Instant dateTo,
        InventoryLogType logType,
        Integer sort
) {

    private static final String PARAM_PRODUCT_ID = "product_id";
    private static final String PARAM_DATE_FROM = "date_from";
    private static final String PARAM_DATE_TO = "date_to";
    private static final String PARAM_LOG_TYPE = "log_type";
    private static final String PARAM_SORT = "sort";
    private static final String PARAM_PAGE = "page";

    public GetInventoryProductLogsRequest {
        Objects.requireNonNull(productId, "productId must not be null");
    }

    public static Builder builder() {
        return new Builder();
    }

    public Map<String, Object> toParams(int page) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put(PARAM_PRODUCT_ID, productId);
        if (dateFrom != null) {
            params.put(PARAM_DATE_FROM, dateFrom.getEpochSecond());
        }
        if (dateTo != null) {
            params.put(PARAM_DATE_TO, dateTo.getEpochSecond());
        }
        if (logType != null) {
            params.put(PARAM_LOG_TYPE, logType.wireValue());
        }
        if (sort != null) {
            params.put(PARAM_SORT, sort);
        }
        params.put(PARAM_PAGE, page);
        return params;
    }

    public static final class Builder {
        private String productId;
        private Instant dateFrom;
        private Instant dateTo;
        private InventoryLogType logType;
        private Integer sort;

        private Builder() {
        }

        public Builder productId(String productId) {
            this.productId = productId;
            return this;
        }

        public Builder dateFrom(Instant dateFrom) {
            this.dateFrom = dateFrom;
            return this;
        }

        public Builder dateTo(Instant dateTo) {
            this.dateTo = dateTo;
            return this;
        }

        public Builder logType(InventoryLogType logType) {
            this.logType = logType;
            return this;
        }

        public Builder sort(int sort) {
            this.sort = sort;
            return this;
        }

        public GetInventoryProductLogsRequest build() {
            return new GetInventoryProductLogsRequest(productId, dateFrom, dateTo, logType, sort);
        }
    }
}
