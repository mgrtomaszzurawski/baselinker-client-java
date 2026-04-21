package io.github.mgrtomaszzurawski.baselinker.demo;

import io.github.mgrtomaszzurawski.baselinker.client.BaselinkerClient;
import io.github.mgrtomaszzurawski.baselinker.client.documents.AddInventoryDocumentItemsResult;
import io.github.mgrtomaszzurawski.baselinker.client.documents.AddInventoryDocumentRequest;
import io.github.mgrtomaszzurawski.baselinker.client.documents.AddInventoryDocumentResult;
import io.github.mgrtomaszzurawski.baselinker.client.documents.InventoryDocumentItem;
import io.github.mgrtomaszzurawski.baselinker.client.documents.InventoryDocumentType;
import io.github.mgrtomaszzurawski.baselinker.client.documents.model.InventoryDocumentSeries;
import io.github.mgrtomaszzurawski.baselinker.client.inventory.model.Inventory;
import io.github.mgrtomaszzurawski.baselinker.client.inventory.model.InventoryProduct;
import io.github.mgrtomaszzurawski.baselinker.client.inventory.model.InventoryProductData;
import io.github.mgrtomaszzurawski.baselinker.client.inventory.model.InventoryProductLog;
import io.github.mgrtomaszzurawski.baselinker.client.inventory.model.InventoryStockEntry;
import io.github.mgrtomaszzurawski.baselinker.client.inventory.model.InventoryWarehouse;
import io.github.mgrtomaszzurawski.baselinker.client.inventory.products.GetInventoryProductLogsRequest;
import io.github.mgrtomaszzurawski.baselinker.client.inventory.products.GetInventoryProductsListRequest;
import io.github.mgrtomaszzurawski.baselinker.client.inventory.products.InventoryLogType;
import io.github.mgrtomaszzurawski.baselinker.client.inventory.products.UpdateInventoryProductsStockRequest;
import io.github.mgrtomaszzurawski.baselinker.client.inventory.products.UpdateInventoryProductsStockResult;
import io.github.mgrtomaszzurawski.baselinker.client.model.AddOrderResponse;
import io.github.mgrtomaszzurawski.baselinker.client.orders.AddOrderProduct;
import io.github.mgrtomaszzurawski.baselinker.client.orders.AddOrderRequest;
import io.github.mgrtomaszzurawski.baselinker.client.orders.GetOrdersRequest;
import io.github.mgrtomaszzurawski.baselinker.client.orders.model.Order;
import io.github.mgrtomaszzurawski.baselinker.client.orders.model.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Exercises every v1 MVP method end-to-end. READ calls always run; WRITE calls run only
 * when {@link #includeWrites} is {@code true}.
 */
final class DemoRunner {

    private static final long RECENT_ORDER_LIST_DAYS = 30L;
    private static final int RECENT_PRINT_LIMIT = 5;
    private static final String DEMO_WRITE_STORAGE = "db";
    private static final int DEMO_WRITE_QUANTITY = 1;
    private static final BigDecimal DEMO_WRITE_PRICE = new BigDecimal("1.00");

    private final BaselinkerClient client;
    private final boolean includeWrites;

    DemoRunner(BaselinkerClient client, boolean includeWrites) {
        this.client = Objects.requireNonNull(client, "client must not be null");
        this.includeWrites = includeWrites;
    }

    void run() {
        banner("READ: orders");
        demoOrderStatuses();
        demoListOrders();

        banner("READ: inventories");
        Inventory firstInventory = demoCatalogs();
        demoWarehouses();

        if (firstInventory == null) {
            System.out.println("No inventory available — skipping product-level READ demos.");
        } else {
            banner("READ: inventory products");
            InventoryProduct firstProduct = demoProductList(firstInventory.inventoryId());
            demoProductStock(firstInventory.inventoryId());
            if (firstProduct != null) {
                demoProductData(firstInventory.inventoryId(), firstProduct.id());
                demoProductLogs(firstProduct.id());
            }
        }

        banner("READ: inventory document series");
        demoDocumentSeries();

        if (!includeWrites) {
            System.out.println();
            System.out.println("Set BASELINKER_DEMO_WRITES=true to also exercise WRITE methods.");
            return;
        }

        banner("WRITE: order flow");
        demoWriteOrderFlow();

        if (firstInventory != null) {
            banner("WRITE: stock update (idempotent +0 delta)");
            demoWriteStockUpdate(firstInventory.inventoryId());

            banner("WRITE: inventory document GRN flow");
            demoWriteDocumentFlow(firstInventory);
        }
    }

    private void demoOrderStatuses() {
        List<OrderStatus> statuses = client.orders().statusList();
        System.out.println("Order statuses (" + statuses.size() + "):");
        statuses.stream().limit(RECENT_PRINT_LIMIT)
                .forEach(status -> System.out.println("  " + status.id() + " — " + status.name()));
    }

    private void demoListOrders() {
        Instant since = Instant.now().minus(RECENT_ORDER_LIST_DAYS, ChronoUnit.DAYS);
        long count = client.orders().list(
                GetOrdersRequest.builder().dateConfirmedFrom(since).build()
        ).stream().limit(RECENT_PRINT_LIMIT).count();
        System.out.println("Recent orders sampled: " + count);
    }

    private Inventory demoCatalogs() {
        List<Inventory> catalogs = client.inventory().catalogs();
        System.out.println("Inventories (" + catalogs.size() + "):");
        catalogs.forEach(inventory -> System.out.println(
                "  " + inventory.inventoryId() + " — " + inventory.name()));
        return catalogs.isEmpty() ? null : catalogs.get(0);
    }

    private void demoWarehouses() {
        List<InventoryWarehouse> warehouses = client.inventory().warehouses();
        System.out.println("Warehouses (" + warehouses.size() + "):");
        warehouses.stream().limit(RECENT_PRINT_LIMIT)
                .forEach(warehouse -> System.out.println(
                        "  " + warehouse.warehouseId() + " — " + warehouse.name()
                                + " (" + warehouse.warehouseType() + ")"));
    }

    private InventoryProduct demoProductList(long inventoryId) {
        List<InventoryProduct> sample = client.inventory().products()
                .list(GetInventoryProductsListRequest.of(inventoryId))
                .stream().limit(RECENT_PRINT_LIMIT).toList();
        System.out.println("Products sampled: " + sample.size());
        sample.forEach(product -> System.out.println(
                "  " + product.id() + " sku=" + product.sku() + " name=" + product.name()));
        return sample.isEmpty() ? null : sample.get(0);
    }

    private void demoProductStock(long inventoryId) {
        long count = client.inventory().products()
                .stock(inventoryId)
                .stream().limit(RECENT_PRINT_LIMIT).count();
        System.out.println("Stock entries sampled: " + count);
    }

    private void demoProductData(long inventoryId, String productId) {
        Map<String, InventoryProductData> data = client.inventory().products()
                .data(inventoryId, List.of(productId));
        InventoryProductData product = data.get(productId);
        System.out.println("Product data for " + productId + ": sku=" + (product == null ? "n/a" : product.sku()));
    }

    private void demoProductLogs(String productId) {
        long count = client.inventory().products()
                .logs(GetInventoryProductLogsRequest.builder()
                        .productId(productId)
                        .logType(InventoryLogType.STOCK)
                        .build())
                .stream().limit(RECENT_PRINT_LIMIT).count();
        System.out.println("Stock log entries sampled for " + productId + ": " + count);
    }

    private void demoDocumentSeries() {
        List<InventoryDocumentSeries> series = client.inventoryDocuments().series();
        System.out.println("Document series (" + series.size() + "):");
        series.stream().limit(RECENT_PRINT_LIMIT)
                .forEach(entry -> System.out.println(
                        "  " + entry.documentSeriesId() + " — " + entry.name()
                                + " (" + entry.type() + ")"));
    }

    private void demoWriteOrderFlow() {
        List<OrderStatus> statuses = client.orders().statusList();
        if (statuses.isEmpty()) {
            System.out.println("Cannot create order: no order statuses configured.");
            return;
        }
        long firstStatus = statuses.get(0).id();

        AddOrderResponse created = client.orders().add(AddOrderRequest.builder()
                .orderStatusId(firstStatus)
                .dateAdd(Instant.now())
                .currency("PLN")
                .userComments("SDK demo — safe to delete")
                .products(List.of(AddOrderProduct.builder()
                        .storage(DEMO_WRITE_STORAGE)
                        .productId("demo-product")
                        .quantity(DEMO_WRITE_QUANTITY)
                        .priceBrutto(DEMO_WRITE_PRICE)
                        .build()))
                .build());
        System.out.println("Created demo order id=" + created.getOrderId());

        client.orders().setStatus(created.getOrderId(), firstStatus);
        System.out.println("Re-set status to same id — exercised setOrderStatus.");
    }

    private void demoWriteStockUpdate(long inventoryId) {
        List<InventoryStockEntry> sample = client.inventory().products()
                .stock(inventoryId)
                .stream().limit(1).toList();
        if (sample.isEmpty()) {
            System.out.println("No stock entries to update — skipping stock write demo.");
            return;
        }
        InventoryStockEntry first = sample.get(0);
        Map<String, Integer> existing = first.stockByWarehouse();
        if (existing == null || existing.isEmpty()) {
            System.out.println("Product has no warehouse stock — skipping stock write demo.");
            return;
        }
        Map.Entry<String, Integer> anyWarehouse = existing.entrySet().iterator().next();
        UpdateInventoryProductsStockResult result = client.inventory().products().updateStock(
                UpdateInventoryProductsStockRequest.of(inventoryId,
                        Map.of(first.productId(),
                                Map.of(anyWarehouse.getKey(), anyWarehouse.getValue()))));
        System.out.println("Stock update counter: " + result.counter()
                + ", warnings: " + result.warningsOrEmpty().size());
    }

    private void demoWriteDocumentFlow(Inventory inventory) {
        if (inventory.warehouses() == null || inventory.warehouses().isEmpty()) {
            System.out.println("Inventory has no warehouses — skipping document demo.");
            return;
        }
        long firstWarehouseId;
        try {
            firstWarehouseId = Long.parseLong(inventory.warehouses().get(0));
        } catch (NumberFormatException ignored) {
            System.out.println("Warehouse id is not numeric — skipping document demo.");
            return;
        }

        AddInventoryDocumentResult created = client.inventoryDocuments().add(
                AddInventoryDocumentRequest.builder()
                        .warehouseId(firstWarehouseId)
                        .documentType(InventoryDocumentType.GOODS_RECEIPT)
                        .dateAdd(Instant.now())
                        .notes("SDK demo — safe to delete")
                        .build());
        System.out.println("Created draft document id=" + created.documentId()
                + ", number=" + created.documentNumber());

        AddInventoryDocumentItemsResult items = client.inventoryDocuments().addItems(
                created.documentId(),
                List.of(InventoryDocumentItem.builder()
                        .productId(1L)
                        .quantity(DEMO_WRITE_QUANTITY)
                        .price(DEMO_WRITE_PRICE)
                        .build()));
        System.out.println("Document items added: " + items.itemsOrEmpty().size());

        System.out.println("Skipping confirm() — would move stock. "
                + "Delete the draft from the BaseLinker panel.");
    }

    private static void banner(String label) {
        System.out.println();
        System.out.println("== " + label + " ==");
    }
}
