package io.github.mgrtomaszzurawski.baselinker.client.inventory.products;

import io.github.mgrtomaszzurawski.baselinker.client.BaselinkerClient;
import io.github.mgrtomaszzurawski.baselinker.client.BaselinkerException;
import io.github.mgrtomaszzurawski.baselinker.client.inventory.model.InventoryProduct;
import io.github.mgrtomaszzurawski.baselinker.client.inventory.model.InventoryProductData;
import io.github.mgrtomaszzurawski.baselinker.client.inventory.model.InventoryProductLog;
import io.github.mgrtomaszzurawski.baselinker.client.inventory.model.InventoryStockEntry;
import io.github.mgrtomaszzurawski.baselinker.client.pagination.PageNumberedResultPage;
import io.github.mgrtomaszzurawski.baselinker.client.pagination.ResultPage;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Typed access to BaseLinker inventory product methods. Obtain via
 * {@code client.inventory().products()}. Wraps {@code getInventoryProductsList},
 * {@code getInventoryProductsData}, {@code getInventoryProductsStock},
 * {@code updateInventoryProductsStock}, and {@code getInventoryProductLogs}.
 */
public final class InventoryProductsService {

    private static final String METHOD_GET_INVENTORY_PRODUCTS_LIST = "getInventoryProductsList";
    private static final String METHOD_GET_INVENTORY_PRODUCTS_DATA = "getInventoryProductsData";
    private static final String METHOD_GET_INVENTORY_PRODUCTS_STOCK = "getInventoryProductsStock";
    private static final String METHOD_UPDATE_INVENTORY_PRODUCTS_STOCK = "updateInventoryProductsStock";
    private static final String METHOD_GET_INVENTORY_PRODUCT_LOGS = "getInventoryProductLogs";
    private static final String PARAM_INVENTORY_ID = "inventory_id";
    private static final String PARAM_PRODUCTS = "products";
    private static final String PARAM_PAGE = "page";

    private final BaselinkerClient client;

    public InventoryProductsService(BaselinkerClient client) {
        this.client = Objects.requireNonNull(client, "client must not be null");
    }

    /**
     * Streams every product in the catalog matching the filters in {@code request}.
     * Auto-paginates by page number.
     */
    public ResultPage<InventoryProduct> list(GetInventoryProductsListRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new PageNumberedResultPage<>(page -> {
            InventoryProductsListResponse response = client.execute(
                    METHOD_GET_INVENTORY_PRODUCTS_LIST, request.toParams(page),
                    InventoryProductsListResponse.class);
            return response.productsAsList();
        });
    }

    /**
     * Returns detailed data for the given product IDs. Single call — no pagination.
     */
    public Map<String, InventoryProductData> data(long inventoryId, List<String> productIds) {
        Objects.requireNonNull(productIds, "productIds must not be null");
        if (productIds.isEmpty()) {
            throw new IllegalArgumentException("productIds must not be empty");
        }
        Map<String, Object> params = new LinkedHashMap<>();
        params.put(PARAM_INVENTORY_ID, inventoryId);
        params.put(PARAM_PRODUCTS, productIds);
        InventoryProductsDataResponse response = client.execute(
                METHOD_GET_INVENTORY_PRODUCTS_DATA, params,
                InventoryProductsDataResponse.class);
        return response.productsOrEmpty();
    }

    /**
     * Streams every stock entry in the catalog. Auto-paginates by page number.
     */
    public ResultPage<InventoryStockEntry> stock(long inventoryId) {
        return new PageNumberedResultPage<>(page -> {
            Map<String, Object> params = new LinkedHashMap<>();
            params.put(PARAM_INVENTORY_ID, inventoryId);
            params.put(PARAM_PAGE, page);
            InventoryProductsStockResponse response = client.execute(
                    METHOD_GET_INVENTORY_PRODUCTS_STOCK, params,
                    InventoryProductsStockResponse.class);
            return response.productsAsList();
        });
    }

    /**
     * Updates stock levels for up to 1000 products in one call.
     */
    public UpdateInventoryProductsStockResult updateStock(UpdateInventoryProductsStockRequest request)
            throws BaselinkerException {
        Objects.requireNonNull(request, "request must not be null");
        return client.execute(
                METHOD_UPDATE_INVENTORY_PRODUCTS_STOCK, request.toParams(),
                UpdateInventoryProductsStockResult.class);
    }

    /**
     * Streams change events for a single product. Auto-paginates by page number.
     */
    public ResultPage<InventoryProductLog> logs(GetInventoryProductLogsRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new PageNumberedResultPage<>(page -> {
            InventoryProductLogsResponse response = client.execute(
                    METHOD_GET_INVENTORY_PRODUCT_LOGS, request.toParams(page),
                    InventoryProductLogsResponse.class);
            return response.logsOrEmpty();
        });
    }
}
