package com.luxeglow.jewelrybackend.dto;

public class CreatePaymentOrderResponse {
    private String razorpayOrderId;
    private String key;
    private Integer amount;
    private String currency;
    private String customerName;
    private String email;

    public CreatePaymentOrderResponse() {}

    public CreatePaymentOrderResponse(String razorpayOrderId, String key, Integer amount, String currency, String customerName, String email) {
        this.razorpayOrderId = razorpayOrderId;
        this.key = key;
        this.amount = amount;
        this.currency = currency;
        this.customerName = customerName;
        this.email = email;
    }

    public String getRazorpayOrderId() { return razorpayOrderId; }
    public void setRazorpayOrderId(String razorpayOrderId) { this.razorpayOrderId = razorpayOrderId; }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}