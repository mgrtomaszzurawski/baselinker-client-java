package io.github.mgrtomaszzurawski.baselinker.client.documents;

import io.github.mgrtomaszzurawski.baselinker.client.BaselinkerClient;
import io.github.mgrtomaszzurawski.baselinker.client.BaselinkerException;
import io.github.mgrtomaszzurawski.baselinker.client.documents.model.InventoryDocumentSeries;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Typed access to BaseLinker inventory document methods. Obtain via
 * {@code client.inventoryDocuments()}. Wraps the three-step Goods Receipt Note (GRN) flow
 * — {@link #add}, {@link #addItems}, {@link #confirm} — plus {@link #series}.
 */
public final class InventoryDocumentsService {

    private static final String METHOD_ADD_INVENTORY_DOCUMENT = "addInventoryDocument";
    private static final String METHOD_ADD_INVENTORY_DOCUMENT_ITEMS = "addInventoryDocumentItems";
    private static final String METHOD_SET_INVENTORY_DOCUMENT_STATUS_CONFIRMED =
            "setInventoryDocumentStatusConfirmed";
    private static final String METHOD_GET_INVENTORY_DOCUMENT_SERIES = "getInventoryDocumentSeries";
    private static final String PARAM_DOCUMENT_ID = "document_id";
    private static final String PARAM_ITEMS = "items";

    private final BaselinkerClient client;

    public InventoryDocumentsService(BaselinkerClient client) {
        this.client = Objects.requireNonNull(client, "client must not be null");
    }

    /**
     * Creates a draft inventory document. The document is not yet applied to stock —
     * call {@link #addItems} and then {@link #confirm}.
     */
    public AddInventoryDocumentResult add(AddInventoryDocumentRequest request)
            throws BaselinkerException {
        Objects.requireNonNull(request, "request must not be null");
        return client.execute(
                METHOD_ADD_INVENTORY_DOCUMENT, request.toParams(), AddInventoryDocumentResult.class);
    }

    /**
     * Appends items to an existing draft document.
     */
    public AddInventoryDocumentItemsResult addItems(long documentId, List<InventoryDocumentItem> items)
            throws BaselinkerException {
        Objects.requireNonNull(items, "items must not be null");
        if (items.isEmpty()) {
            throw new IllegalArgumentException("items must not be empty");
        }
        List<Map<String, Object>> itemParams = new ArrayList<>(items.size());
        for (InventoryDocumentItem item : items) {
            itemParams.add(item.toParams());
        }
        Map<String, Object> params = new LinkedHashMap<>();
        params.put(PARAM_DOCUMENT_ID, documentId);
        params.put(PARAM_ITEMS, itemParams);
        return client.execute(
                METHOD_ADD_INVENTORY_DOCUMENT_ITEMS, params, AddInventoryDocumentItemsResult.class);
    }

    /**
     * Confirms the draft. This is the step that actually moves stock.
     */
    public void confirm(long documentId) throws BaselinkerException {
        Map<String, Object> params = Map.of(PARAM_DOCUMENT_ID, documentId);
        client.execute(METHOD_SET_INVENTORY_DOCUMENT_STATUS_CONFIRMED, params,
                DocumentStatusOnlyResponse.class);
    }

    /**
     * Returns the document numbering series configured per warehouse.
     */
    public List<InventoryDocumentSeries> series() throws BaselinkerException {
        InventoryDocumentSeriesResponse response = client.execute(
                METHOD_GET_INVENTORY_DOCUMENT_SERIES, InventoryDocumentSeriesResponse.class);
        return response.documentSeriesOrEmpty();
    }
}
