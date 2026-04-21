package io.github.mgrtomaszzurawski.baselinker.examples;

import io.github.mgrtomaszzurawski.baselinker.client.BaselinkerClient;
import io.github.mgrtomaszzurawski.baselinker.client.documents.AddInventoryDocumentItemsResult;
import io.github.mgrtomaszzurawski.baselinker.client.documents.AddInventoryDocumentRequest;
import io.github.mgrtomaszzurawski.baselinker.client.documents.AddInventoryDocumentResult;
import io.github.mgrtomaszzurawski.baselinker.client.documents.InventoryDocumentItem;
import io.github.mgrtomaszzurawski.baselinker.client.documents.InventoryDocumentType;

import java.math.BigDecimal;
import java.util.List;

/**
 * Three-step GRN (Goods Receipt Note) flow: create draft, append items, confirm.
 * The confirmation is what actually moves stock — skip it to keep the document as
 * a draft.
 */
public final class ReceiveGoods {

    private static final long WAREHOUSE_ID = 205L;
    private static final long PRODUCT_ID = 5432L;
    private static final int RECEIVED_QUANTITY = 100;
    private static final BigDecimal UNIT_PRICE = new BigDecimal("9.99");

    private ReceiveGoods() {
    }

    public static void main(String[] args) {
        BaselinkerClient client = BaselinkerClient.builder()
                .apiToken(System.getenv("BASELINKER_TOKEN"))
                .build();

        AddInventoryDocumentResult draft = client.inventoryDocuments().add(
                AddInventoryDocumentRequest.builder()
                        .warehouseId(WAREHOUSE_ID)
                        .documentType(InventoryDocumentType.GOODS_RECEIPT)
                        .invoiceNo("FV/2026/1234")
                        .notes("Shipment from Supplier A")
                        .build());
        long documentId = draft.documentId();
        System.out.println("Draft document id=" + documentId + " number=" + draft.documentNumber());

        AddInventoryDocumentItemsResult items = client.inventoryDocuments().addItems(
                documentId,
                List.of(InventoryDocumentItem.builder()
                        .productId(PRODUCT_ID)
                        .quantity(RECEIVED_QUANTITY)
                        .price(UNIT_PRICE)
                        .build()));
        System.out.println("Items added: " + items.itemsOrEmpty().size());

        client.inventoryDocuments().confirm(documentId);
        System.out.println("Confirmed — stock levels updated.");
    }
}
