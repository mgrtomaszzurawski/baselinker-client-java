package io.github.mgrtomaszzurawski.baselinker.client.orders.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * A BaseLinker order as returned by {@code getOrders}. Fields match the subset of
 * {@code docs/methods/getOrders.md} output fields that the v1 SDK exposes as typed
 * members; any unmapped wire fields are ignored during deserialization.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Order(
        @JsonProperty("order_id") long orderId,
        @JsonProperty("shop_order_id") String shopOrderId,
        @JsonProperty("external_order_id") String externalOrderId,
        @JsonProperty("order_source") String orderSource,
        @JsonProperty("order_source_id") Long orderSourceId,
        @JsonProperty("order_status_id") long orderStatusId,
        @JsonProperty("date_add") Instant dateAdd,
        @JsonProperty("date_confirmed") Instant dateConfirmed,
        @JsonProperty("date_in_status") Instant dateInStatus,
        @JsonProperty("confirmed") Boolean confirmed,
        @JsonProperty("currency") String currency,
        @JsonProperty("payment_method") String paymentMethod,
        @JsonProperty("payment_method_cod") Boolean paymentMethodCod,
        @JsonProperty("payment_done") BigDecimal paymentDone,
        @JsonProperty("user_comments") String userComments,
        @JsonProperty("admin_comments") String adminComments,
        @JsonProperty("email") String email,
        @JsonProperty("phone") String phone,
        @JsonProperty("user_login") String userLogin,
        @JsonProperty("delivery_method") String deliveryMethod,
        @JsonProperty("delivery_price") BigDecimal deliveryPrice,
        @JsonProperty("delivery_fullname") String deliveryFullname,
        @JsonProperty("delivery_company") String deliveryCompany,
        @JsonProperty("delivery_address") String deliveryAddress,
        @JsonProperty("delivery_postcode") String deliveryPostcode,
        @JsonProperty("delivery_city") String deliveryCity,
        @JsonProperty("delivery_state") String deliveryState,
        @JsonProperty("delivery_country_code") String deliveryCountryCode,
        @JsonProperty("invoice_fullname") String invoiceFullname,
        @JsonProperty("invoice_company") String invoiceCompany,
        @JsonProperty("invoice_nip") String invoiceNip,
        @JsonProperty("invoice_address") String invoiceAddress,
        @JsonProperty("invoice_postcode") String invoicePostcode,
        @JsonProperty("invoice_city") String invoiceCity,
        @JsonProperty("invoice_country_code") String invoiceCountryCode,
        @JsonProperty("want_invoice") Boolean wantInvoice,
        @JsonProperty("extra_field_1") String extraField1,
        @JsonProperty("extra_field_2") String extraField2,
        @JsonProperty("products") List<OrderProduct> products
) {
}
