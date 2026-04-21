package io.github.mgrtomaszzurawski.baselinker.client.orders;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Typed request for {@code getOrders}. {@code dateConfirmedFrom} is required — it is the
 * cursor the SDK advances for pagination. Additional fields are optional filters.
 */
public record GetOrdersRequest(
        Instant dateConfirmedFrom,
        Boolean includeUnconfirmed,
        Long statusId
) {

    private static final String PARAM_DATE_CONFIRMED_FROM = "date_confirmed_from";
    private static final String PARAM_GET_UNCONFIRMED_ORDERS = "get_unconfirmed_orders";
    private static final String PARAM_STATUS_ID = "status_id";

    public GetOrdersRequest {
        Objects.requireNonNull(dateConfirmedFrom, "dateConfirmedFrom must not be null");
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Converts the request to the wire-format parameter map, replacing the cursor with
     * the supplied {@code cursor}. The SDK calls this per batch during pagination.
     */
    public Map<String, Object> toParams(Instant cursor) {
        Objects.requireNonNull(cursor, "cursor must not be null");
        Map<String, Object> params = new LinkedHashMap<>();
        params.put(PARAM_DATE_CONFIRMED_FROM, cursor.getEpochSecond());
        if (includeUnconfirmed != null) {
            params.put(PARAM_GET_UNCONFIRMED_ORDERS, includeUnconfirmed);
        }
        if (statusId != null) {
            params.put(PARAM_STATUS_ID, statusId);
        }
        return params;
    }

    public static final class Builder {
        private Instant dateConfirmedFrom;
        private Boolean includeUnconfirmed;
        private Long statusId;

        private Builder() {
        }

        public Builder dateConfirmedFrom(Instant dateConfirmedFrom) {
            this.dateConfirmedFrom = dateConfirmedFrom;
            return this;
        }

        public Builder includeUnconfirmed(boolean includeUnconfirmed) {
            this.includeUnconfirmed = includeUnconfirmed;
            return this;
        }

        public Builder statusId(long statusId) {
            this.statusId = statusId;
            return this;
        }

        public GetOrdersRequest build() {
            return new GetOrdersRequest(dateConfirmedFrom, includeUnconfirmed, statusId);
        }
    }
}
