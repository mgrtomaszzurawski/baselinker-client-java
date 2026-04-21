package io.github.mgrtomaszzurawski.baselinker.client.documents;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * One item added to an inventory document via
 * {@code addInventoryDocumentItems}. {@code productId} and {@code quantity} are required.
 */
public record InventoryDocumentItem(
        long productId,
        int quantity,
        BigDecimal price,
        String locationName,
        String targetLocationName,
        LocalDate expiryDate,
        String batch,
        String serialNo,
        String comments
) {

    private static final String PARAM_PRODUCT_ID = "product_id";
    private static final String PARAM_QUANTITY = "quantity";
    private static final String PARAM_PRICE = "price";
    private static final String PARAM_LOCATION_NAME = "location_name";
    private static final String PARAM_TARGET_LOCATION_NAME = "target_location_name";
    private static final String PARAM_EXPIRY_DATE = "expiry_date";
    private static final String PARAM_BATCH = "batch";
    private static final String PARAM_SERIAL_NO = "serial_no";
    private static final String PARAM_COMMENTS = "comments";

    public InventoryDocumentItem {
        if (productId <= 0) {
            throw new IllegalArgumentException("productId must be positive");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public Map<String, Object> toParams() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put(PARAM_PRODUCT_ID, productId);
        params.put(PARAM_QUANTITY, quantity);
        putIfNotNull(params, PARAM_PRICE, price);
        putIfNotNull(params, PARAM_LOCATION_NAME, locationName);
        putIfNotNull(params, PARAM_TARGET_LOCATION_NAME, targetLocationName);
        if (expiryDate != null) {
            params.put(PARAM_EXPIRY_DATE, expiryDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        putIfNotNull(params, PARAM_BATCH, batch);
        putIfNotNull(params, PARAM_SERIAL_NO, serialNo);
        putIfNotNull(params, PARAM_COMMENTS, comments);
        return params;
    }

    private static void putIfNotNull(Map<String, Object> target, String key, Object value) {
        if (value != null) {
            target.put(key, value);
        }
    }

    public static final class Builder {
        private long productId;
        private int quantity;
        private BigDecimal price;
        private String locationName;
        private String targetLocationName;
        private LocalDate expiryDate;
        private String batch;
        private String serialNo;
        private String comments;

        private Builder() {
        }

        public Builder productId(long productId) {
            this.productId = productId;
            return this;
        }

        public Builder quantity(int quantity) {
            this.quantity = quantity;
            return this;
        }

        public Builder price(BigDecimal price) {
            this.price = Objects.requireNonNull(price, "price must not be null");
            return this;
        }

        public Builder locationName(String locationName) {
            this.locationName = locationName;
            return this;
        }

        public Builder targetLocationName(String targetLocationName) {
            this.targetLocationName = targetLocationName;
            return this;
        }

        public Builder expiryDate(LocalDate expiryDate) {
            this.expiryDate = expiryDate;
            return this;
        }

        public Builder batch(String batch) {
            this.batch = batch;
            return this;
        }

        public Builder serialNo(String serialNo) {
            this.serialNo = serialNo;
            return this;
        }

        public Builder comments(String comments) {
            this.comments = comments;
            return this;
        }

        public InventoryDocumentItem build() {
            return new InventoryDocumentItem(productId, quantity, price, locationName,
                    targetLocationName, expiryDate, batch, serialNo, comments);
        }
    }
}
