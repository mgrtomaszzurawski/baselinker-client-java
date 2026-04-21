package io.github.mgrtomaszzurawski.baselinker.client.orders;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Typed request for {@code addOrder}. {@code orderStatusId}, {@code dateAdd},
 * {@code currency} and a non-empty {@code products} list are required; the rest are
 * optional. Fields not modeled here can be supplied via {@code extraParams}.
 */
public record AddOrderRequest(
        long orderStatusId,
        Instant dateAdd,
        String currency,
        String paymentMethod,
        Boolean paymentMethodCod,
        Boolean paid,
        String userComments,
        String adminComments,
        String email,
        String phone,
        String userLogin,
        String deliveryMethod,
        BigDecimal deliveryPrice,
        String deliveryFullname,
        String deliveryCompany,
        String deliveryAddress,
        String deliveryPostcode,
        String deliveryCity,
        String deliveryState,
        String deliveryCountryCode,
        String invoiceFullname,
        String invoiceCompany,
        String invoiceNip,
        String invoiceAddress,
        String invoicePostcode,
        String invoiceCity,
        String invoiceCountryCode,
        Boolean wantInvoice,
        String extraField1,
        String extraField2,
        List<AddOrderProduct> products,
        Map<String, Object> extraParams
) {

    private static final String PARAM_ORDER_STATUS_ID = "order_status_id";
    private static final String PARAM_DATE_ADD = "date_add";
    private static final String PARAM_CURRENCY = "currency";
    private static final String PARAM_PAYMENT_METHOD = "payment_method";
    private static final String PARAM_PAYMENT_METHOD_COD = "payment_method_cod";
    private static final String PARAM_PAID = "paid";
    private static final String PARAM_USER_COMMENTS = "user_comments";
    private static final String PARAM_ADMIN_COMMENTS = "admin_comments";
    private static final String PARAM_EMAIL = "email";
    private static final String PARAM_PHONE = "phone";
    private static final String PARAM_USER_LOGIN = "user_login";
    private static final String PARAM_DELIVERY_METHOD = "delivery_method";
    private static final String PARAM_DELIVERY_PRICE = "delivery_price";
    private static final String PARAM_DELIVERY_FULLNAME = "delivery_fullname";
    private static final String PARAM_DELIVERY_COMPANY = "delivery_company";
    private static final String PARAM_DELIVERY_ADDRESS = "delivery_address";
    private static final String PARAM_DELIVERY_POSTCODE = "delivery_postcode";
    private static final String PARAM_DELIVERY_CITY = "delivery_city";
    private static final String PARAM_DELIVERY_STATE = "delivery_state";
    private static final String PARAM_DELIVERY_COUNTRY_CODE = "delivery_country_code";
    private static final String PARAM_INVOICE_FULLNAME = "invoice_fullname";
    private static final String PARAM_INVOICE_COMPANY = "invoice_company";
    private static final String PARAM_INVOICE_NIP = "invoice_nip";
    private static final String PARAM_INVOICE_ADDRESS = "invoice_address";
    private static final String PARAM_INVOICE_POSTCODE = "invoice_postcode";
    private static final String PARAM_INVOICE_CITY = "invoice_city";
    private static final String PARAM_INVOICE_COUNTRY_CODE = "invoice_country_code";
    private static final String PARAM_WANT_INVOICE = "want_invoice";
    private static final String PARAM_EXTRA_FIELD_1 = "extra_field_1";
    private static final String PARAM_EXTRA_FIELD_2 = "extra_field_2";
    private static final String PARAM_PRODUCTS = "products";

    public AddOrderRequest {
        Objects.requireNonNull(dateAdd, "dateAdd must not be null");
        Objects.requireNonNull(currency, "currency must not be null");
        Objects.requireNonNull(products, "products must not be null");
        if (products.isEmpty()) {
            throw new IllegalArgumentException("products must not be empty");
        }
        products = List.copyOf(products);
        extraParams = extraParams == null ? Map.of() : Map.copyOf(extraParams);
    }

    public static Builder builder() {
        return new Builder();
    }

    public Map<String, Object> toParams() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put(PARAM_ORDER_STATUS_ID, orderStatusId);
        params.put(PARAM_DATE_ADD, dateAdd.getEpochSecond());
        params.put(PARAM_CURRENCY, currency);
        putIfNotNull(params, PARAM_PAYMENT_METHOD, paymentMethod);
        putIfNotNull(params, PARAM_PAYMENT_METHOD_COD, paymentMethodCod);
        putIfNotNull(params, PARAM_PAID, paid);
        putIfNotNull(params, PARAM_USER_COMMENTS, userComments);
        putIfNotNull(params, PARAM_ADMIN_COMMENTS, adminComments);
        putIfNotNull(params, PARAM_EMAIL, email);
        putIfNotNull(params, PARAM_PHONE, phone);
        putIfNotNull(params, PARAM_USER_LOGIN, userLogin);
        putIfNotNull(params, PARAM_DELIVERY_METHOD, deliveryMethod);
        putIfNotNull(params, PARAM_DELIVERY_PRICE, deliveryPrice);
        putIfNotNull(params, PARAM_DELIVERY_FULLNAME, deliveryFullname);
        putIfNotNull(params, PARAM_DELIVERY_COMPANY, deliveryCompany);
        putIfNotNull(params, PARAM_DELIVERY_ADDRESS, deliveryAddress);
        putIfNotNull(params, PARAM_DELIVERY_POSTCODE, deliveryPostcode);
        putIfNotNull(params, PARAM_DELIVERY_CITY, deliveryCity);
        putIfNotNull(params, PARAM_DELIVERY_STATE, deliveryState);
        putIfNotNull(params, PARAM_DELIVERY_COUNTRY_CODE, deliveryCountryCode);
        putIfNotNull(params, PARAM_INVOICE_FULLNAME, invoiceFullname);
        putIfNotNull(params, PARAM_INVOICE_COMPANY, invoiceCompany);
        putIfNotNull(params, PARAM_INVOICE_NIP, invoiceNip);
        putIfNotNull(params, PARAM_INVOICE_ADDRESS, invoiceAddress);
        putIfNotNull(params, PARAM_INVOICE_POSTCODE, invoicePostcode);
        putIfNotNull(params, PARAM_INVOICE_CITY, invoiceCity);
        putIfNotNull(params, PARAM_INVOICE_COUNTRY_CODE, invoiceCountryCode);
        putIfNotNull(params, PARAM_WANT_INVOICE, wantInvoice);
        putIfNotNull(params, PARAM_EXTRA_FIELD_1, extraField1);
        putIfNotNull(params, PARAM_EXTRA_FIELD_2, extraField2);
        List<Map<String, Object>> productParams = new ArrayList<>(products.size());
        for (AddOrderProduct product : products) {
            productParams.add(product.toParams());
        }
        params.put(PARAM_PRODUCTS, productParams);
        params.putAll(extraParams);
        return params;
    }

    private static void putIfNotNull(Map<String, Object> target, String key, Object value) {
        if (value != null) {
            target.put(key, value);
        }
    }

    public static final class Builder {
        private long orderStatusId;
        private Instant dateAdd;
        private String currency;
        private String paymentMethod;
        private Boolean paymentMethodCod;
        private Boolean paid;
        private String userComments;
        private String adminComments;
        private String email;
        private String phone;
        private String userLogin;
        private String deliveryMethod;
        private BigDecimal deliveryPrice;
        private String deliveryFullname;
        private String deliveryCompany;
        private String deliveryAddress;
        private String deliveryPostcode;
        private String deliveryCity;
        private String deliveryState;
        private String deliveryCountryCode;
        private String invoiceFullname;
        private String invoiceCompany;
        private String invoiceNip;
        private String invoiceAddress;
        private String invoicePostcode;
        private String invoiceCity;
        private String invoiceCountryCode;
        private Boolean wantInvoice;
        private String extraField1;
        private String extraField2;
        private List<AddOrderProduct> products = Collections.emptyList();
        private Map<String, Object> extraParams = Map.of();

        private Builder() {
        }

        public Builder orderStatusId(long orderStatusId) {
            this.orderStatusId = orderStatusId;
            return this;
        }

        public Builder dateAdd(Instant dateAdd) {
            this.dateAdd = dateAdd;
            return this;
        }

        public Builder currency(String currency) {
            this.currency = currency;
            return this;
        }

        public Builder paymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
            return this;
        }

        public Builder paymentMethodCod(boolean paymentMethodCod) {
            this.paymentMethodCod = paymentMethodCod;
            return this;
        }

        public Builder paid(boolean paid) {
            this.paid = paid;
            return this;
        }

        public Builder userComments(String userComments) {
            this.userComments = userComments;
            return this;
        }

        public Builder adminComments(String adminComments) {
            this.adminComments = adminComments;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public Builder userLogin(String userLogin) {
            this.userLogin = userLogin;
            return this;
        }

        public Builder deliveryMethod(String deliveryMethod) {
            this.deliveryMethod = deliveryMethod;
            return this;
        }

        public Builder deliveryPrice(BigDecimal deliveryPrice) {
            this.deliveryPrice = deliveryPrice;
            return this;
        }

        public Builder deliveryFullname(String deliveryFullname) {
            this.deliveryFullname = deliveryFullname;
            return this;
        }

        public Builder deliveryCompany(String deliveryCompany) {
            this.deliveryCompany = deliveryCompany;
            return this;
        }

        public Builder deliveryAddress(String deliveryAddress) {
            this.deliveryAddress = deliveryAddress;
            return this;
        }

        public Builder deliveryPostcode(String deliveryPostcode) {
            this.deliveryPostcode = deliveryPostcode;
            return this;
        }

        public Builder deliveryCity(String deliveryCity) {
            this.deliveryCity = deliveryCity;
            return this;
        }

        public Builder deliveryState(String deliveryState) {
            this.deliveryState = deliveryState;
            return this;
        }

        public Builder deliveryCountryCode(String deliveryCountryCode) {
            this.deliveryCountryCode = deliveryCountryCode;
            return this;
        }

        public Builder invoiceFullname(String invoiceFullname) {
            this.invoiceFullname = invoiceFullname;
            return this;
        }

        public Builder invoiceCompany(String invoiceCompany) {
            this.invoiceCompany = invoiceCompany;
            return this;
        }

        public Builder invoiceNip(String invoiceNip) {
            this.invoiceNip = invoiceNip;
            return this;
        }

        public Builder invoiceAddress(String invoiceAddress) {
            this.invoiceAddress = invoiceAddress;
            return this;
        }

        public Builder invoicePostcode(String invoicePostcode) {
            this.invoicePostcode = invoicePostcode;
            return this;
        }

        public Builder invoiceCity(String invoiceCity) {
            this.invoiceCity = invoiceCity;
            return this;
        }

        public Builder invoiceCountryCode(String invoiceCountryCode) {
            this.invoiceCountryCode = invoiceCountryCode;
            return this;
        }

        public Builder wantInvoice(boolean wantInvoice) {
            this.wantInvoice = wantInvoice;
            return this;
        }

        public Builder extraField1(String extraField1) {
            this.extraField1 = extraField1;
            return this;
        }

        public Builder extraField2(String extraField2) {
            this.extraField2 = extraField2;
            return this;
        }

        public Builder products(List<AddOrderProduct> products) {
            this.products = Objects.requireNonNull(products, "products must not be null");
            return this;
        }

        public Builder extraParams(Map<String, Object> extraParams) {
            this.extraParams = Objects.requireNonNull(extraParams, "extraParams must not be null");
            return this;
        }

        public AddOrderRequest build() {
            return new AddOrderRequest(orderStatusId, dateAdd, currency, paymentMethod, paymentMethodCod,
                    paid, userComments, adminComments, email, phone, userLogin, deliveryMethod,
                    deliveryPrice, deliveryFullname, deliveryCompany, deliveryAddress, deliveryPostcode,
                    deliveryCity, deliveryState, deliveryCountryCode, invoiceFullname, invoiceCompany,
                    invoiceNip, invoiceAddress, invoicePostcode, invoiceCity, invoiceCountryCode,
                    wantInvoice, extraField1, extraField2, products, extraParams);
        }
    }
}
