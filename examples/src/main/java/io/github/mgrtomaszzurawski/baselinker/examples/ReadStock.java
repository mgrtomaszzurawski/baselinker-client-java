package io.github.mgrtomaszzurawski.baselinker.examples;

import io.github.mgrtomaszzurawski.baselinker.client.BaselinkerClient;
import io.github.mgrtomaszzurawski.baselinker.client.inventory.model.InventoryStockEntry;

import java.util.Map;

/**
 * Stream every stock entry in a catalog. The SDK hides page-number pagination behind
 * the returned {@code ResultPage} — the caller just iterates.
 */
public final class ReadStock {

    private static final long INVENTORY_ID = 307L;

    private ReadStock() {
    }

    public static void main(String[] args) {
        BaselinkerClient client = BaselinkerClient.builder()
                .apiToken(System.getenv("BASELINKER_TOKEN"))
                .build();

        client.inventory()
                .products()
                .stock(INVENTORY_ID)
                .stream()
                .forEach(ReadStock::printEntry);
    }

    private static void printEntry(InventoryStockEntry entry) {
        Map<String, Integer> byWarehouse = entry.stockByWarehouse();
        int total = byWarehouse == null
                ? 0
                : byWarehouse.values().stream().mapToInt(Integer::intValue).sum();
        System.out.printf("product %s total stock = %d%n", entry.productId(), total);
    }
}
