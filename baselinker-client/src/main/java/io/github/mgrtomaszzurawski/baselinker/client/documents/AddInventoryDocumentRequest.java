package io.github.mgrtomaszzurawski.baselinker.client.documents;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Typed request for {@code addInventoryDocument}. {@code warehouseId} and
 * {@code documentType} are required; all others optional.
 */
public record AddInventoryDocumentRequest(
        long warehouseId,
        InventoryDocumentType documentType,
        Long targetWarehouseId,
        Instant dateAdd,
        Instant dateExecute,
        String contractor,
        String invoiceNo,
        String notes
) {

    private static final String PARAM_WAREHOUSE_ID = "warehouse_id";
    private static final String PARAM_DOCUMENT_TYPE = "document_type";
    private static final String PARAM_TARGET_WAREHOUSE_ID = "target_warehouse_id";
    private static final String PARAM_DATE_ADD = "date_add";
    private static final String PARAM_DATE_EXECUTE = "date_execute";
    private static final String PARAM_CONTRACTOR = "contractor";
    private static final String PARAM_INVOICE_NO = "invoice_no";
    private static final String PARAM_NOTES = "notes";

    public AddInventoryDocumentRequest {
        Objects.requireNonNull(documentType, "documentType must not be null");
    }

    public static Builder builder() {
        return new Builder();
    }

    public Map<String, Object> toParams() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put(PARAM_WAREHOUSE_ID, warehouseId);
        params.put(PARAM_DOCUMENT_TYPE, documentType.wireValue());
        if (targetWarehouseId != null) {
            params.put(PARAM_TARGET_WAREHOUSE_ID, targetWarehouseId);
        }
        if (dateAdd != null) {
            params.put(PARAM_DATE_ADD, dateAdd.getEpochSecond());
        }
        if (dateExecute != null) {
            params.put(PARAM_DATE_EXECUTE, dateExecute.getEpochSecond());
        }
        if (contractor != null) {
            params.put(PARAM_CONTRACTOR, contractor);
        }
        if (invoiceNo != null) {
            params.put(PARAM_INVOICE_NO, invoiceNo);
        }
        if (notes != null) {
            params.put(PARAM_NOTES, notes);
        }
        return params;
    }

    public static final class Builder {
        private long warehouseId;
        private InventoryDocumentType documentType;
        private Long targetWarehouseId;
        private Instant dateAdd;
        private Instant dateExecute;
        private String contractor;
        private String invoiceNo;
        private String notes;

        private Builder() {
        }

        public Builder warehouseId(long warehouseId) {
            this.warehouseId = warehouseId;
            return this;
        }

        public Builder documentType(InventoryDocumentType documentType) {
            this.documentType = documentType;
            return this;
        }

        public Builder targetWarehouseId(long targetWarehouseId) {
            this.targetWarehouseId = targetWarehouseId;
            return this;
        }

        public Builder dateAdd(Instant dateAdd) {
            this.dateAdd = dateAdd;
            return this;
        }

        public Builder dateExecute(Instant dateExecute) {
            this.dateExecute = dateExecute;
            return this;
        }

        public Builder contractor(String contractor) {
            this.contractor = contractor;
            return this;
        }

        public Builder invoiceNo(String invoiceNo) {
            this.invoiceNo = invoiceNo;
            return this;
        }

        public Builder notes(String notes) {
            this.notes = notes;
            return this;
        }

        public AddInventoryDocumentRequest build() {
            return new AddInventoryDocumentRequest(warehouseId, documentType, targetWarehouseId,
                    dateAdd, dateExecute, contractor, invoiceNo, notes);
        }
    }
}
