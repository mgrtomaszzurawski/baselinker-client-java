package io.github.mgrtomaszzurawski.baselinker.client.inventory.products;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Typed request for {@code getInventoryProductsList}. {@code inventoryId} is required;
 * all other fields are optional filters.
 */
public record GetInventoryProductsListRequest(
        long inventoryId,
        Long filterId,
        Long filterCategoryId,
        String filterEan,
        String filterSku,
        String filterName,
        BigDecimal filterPriceFrom,
        BigDecimal filterPriceTo,
        Integer filterStockFrom,
        Integer filterStockTo,
        String filterSort,
        Boolean includeVariants
) {

    private static final String PARAM_INVENTORY_ID = "inventory_id";
    private static final String PARAM_FILTER_ID = "filter_id";
    private static final String PARAM_FILTER_CATEGORY_ID = "filter_category_id";
    private static final String PARAM_FILTER_EAN = "filter_ean";
    private static final String PARAM_FILTER_SKU = "filter_sku";
    private static final String PARAM_FILTER_NAME = "filter_name";
    private static final String PARAM_FILTER_PRICE_FROM = "filter_price_from";
    private static final String PARAM_FILTER_PRICE_TO = "filter_price_to";
    private static final String PARAM_FILTER_STOCK_FROM = "filter_stock_from";
    private static final String PARAM_FILTER_STOCK_TO = "filter_stock_to";
    private static final String PARAM_FILTER_SORT = "filter_sort";
    private static final String PARAM_INCLUDE_VARIANTS = "include_variants";
    private static final String PARAM_PAGE = "page";

    public static GetInventoryProductsListRequest of(long inventoryId) {
        return builder().inventoryId(inventoryId).build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public Map<String, Object> toParams(int page) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put(PARAM_INVENTORY_ID, inventoryId);
        putIfNotNull(params, PARAM_FILTER_ID, filterId);
        putIfNotNull(params, PARAM_FILTER_CATEGORY_ID, filterCategoryId);
        putIfNotNull(params, PARAM_FILTER_EAN, filterEan);
        putIfNotNull(params, PARAM_FILTER_SKU, filterSku);
        putIfNotNull(params, PARAM_FILTER_NAME, filterName);
        putIfNotNull(params, PARAM_FILTER_PRICE_FROM, filterPriceFrom);
        putIfNotNull(params, PARAM_FILTER_PRICE_TO, filterPriceTo);
        putIfNotNull(params, PARAM_FILTER_STOCK_FROM, filterStockFrom);
        putIfNotNull(params, PARAM_FILTER_STOCK_TO, filterStockTo);
        putIfNotNull(params, PARAM_FILTER_SORT, filterSort);
        putIfNotNull(params, PARAM_INCLUDE_VARIANTS, includeVariants);
        params.put(PARAM_PAGE, page);
        return params;
    }

    private static void putIfNotNull(Map<String, Object> target, String key, Object value) {
        if (value != null) {
            target.put(key, value);
        }
    }

    public static final class Builder {
        private long inventoryId;
        private Long filterId;
        private Long filterCategoryId;
        private String filterEan;
        private String filterSku;
        private String filterName;
        private BigDecimal filterPriceFrom;
        private BigDecimal filterPriceTo;
        private Integer filterStockFrom;
        private Integer filterStockTo;
        private String filterSort;
        private Boolean includeVariants;

        private Builder() {
        }

        public Builder inventoryId(long inventoryId) {
            this.inventoryId = inventoryId;
            return this;
        }

        public Builder filterId(long filterId) {
            this.filterId = filterId;
            return this;
        }

        public Builder filterCategoryId(long filterCategoryId) {
            this.filterCategoryId = filterCategoryId;
            return this;
        }

        public Builder filterEan(String filterEan) {
            this.filterEan = filterEan;
            return this;
        }

        public Builder filterSku(String filterSku) {
            this.filterSku = filterSku;
            return this;
        }

        public Builder filterName(String filterName) {
            this.filterName = filterName;
            return this;
        }

        public Builder filterPriceFrom(BigDecimal filterPriceFrom) {
            this.filterPriceFrom = filterPriceFrom;
            return this;
        }

        public Builder filterPriceTo(BigDecimal filterPriceTo) {
            this.filterPriceTo = filterPriceTo;
            return this;
        }

        public Builder filterStockFrom(int filterStockFrom) {
            this.filterStockFrom = filterStockFrom;
            return this;
        }

        public Builder filterStockTo(int filterStockTo) {
            this.filterStockTo = filterStockTo;
            return this;
        }

        public Builder filterSort(String filterSort) {
            this.filterSort = filterSort;
            return this;
        }

        public Builder includeVariants(boolean includeVariants) {
            this.includeVariants = includeVariants;
            return this;
        }

        public GetInventoryProductsListRequest build() {
            return new GetInventoryProductsListRequest(inventoryId, filterId, filterCategoryId,
                    filterEan, filterSku, filterName, filterPriceFrom, filterPriceTo,
                    filterStockFrom, filterStockTo, filterSort, includeVariants);
        }
    }
}
