package com.luxeglow.jewelrybackend.service;

import com.luxeglow.jewelrybackend.dto.CreatePaymentOrderRequest;
import com.luxeglow.jewelrybackend.dto.CreatePaymentOrderResponse;
import com.luxeglow.jewelrybackend.dto.OrderItemRequest;
import com.luxeglow.jewelrybackend.dto.OrderRequest;
import com.luxeglow.jewelrybackend.dto.VerifyPaymentRequest;
import com.luxeglow.jewelrybackend.entity.Order;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class PaymentService {

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    private final OrderService orderService;

    public PaymentService(OrderService orderService) {
        this.orderService = orderService;
    }

    public CreatePaymentOrderResponse createRazorpayOrder(CreatePaymentOrderRequest request) throws Exception {
        validateOrderRequest(request);

        int serverAmountPaise = calculateAmountPaise(request.getItems());
        if (serverAmountPaise <= 0) {
            throw new RuntimeException("Invalid order amount");
        }

        RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", serverAmountPaise);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "lg_" + System.currentTimeMillis());

        JSONObject notes = new JSONObject();
        notes.put("customerName", request.getCustomerName());
        notes.put("email", request.getEmail());
        orderRequest.put("notes", notes);

        com.razorpay.Order razorpayOrder = razorpay.orders.create(orderRequest);

        return new CreatePaymentOrderResponse(
                razorpayOrder.get("id"),
                razorpayKeyId,
                serverAmountPaise,
                "INR",
                request.getCustomerName(),
                request.getEmail()
        );
    }

    public Order verifyAndCreateOrder(VerifyPaymentRequest request) throws Exception {
        validateVerifyRequest(request);

        boolean valid = verifySignature(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature()
        );

        if (!valid) {
            throw new RuntimeException("Payment signature verification failed");
        }

        int serverAmountPaise = calculateAmountPaise(request.getItems());
        double serverAmountRupees = serverAmountPaise / 100.0;

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setCustomerName(request.getCustomerName());
        orderRequest.setEmail(request.getEmail());
        orderRequest.setAddress(request.getAddress());
        orderRequest.setTotalAmount(serverAmountRupees);
        orderRequest.setItems(request.getItems());

        return orderService.placePaidOrder(
                orderRequest,
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId()
        );
    }

    private void validateOrderRequest(CreatePaymentOrderRequest request) {
        if (request == null) {
            throw new RuntimeException("Request is required");
        }
        if (request.getCustomerName() == null || request.getCustomerName().isBlank()) {
            throw new RuntimeException("Customer name is required");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new RuntimeException("Email is required");
        }
        if (request.getAddress() == null || request.getAddress().isBlank()) {
            throw new RuntimeException("Address is required");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("Items are required");
        }
    }

    private void validateVerifyRequest(VerifyPaymentRequest request) {
        if (request == null) {
            throw new RuntimeException("Request is required");
        }
        if (request.getRazorpayOrderId() == null || request.getRazorpayOrderId().isBlank()) {
            throw new RuntimeException("razorpayOrderId is required");
        }
        if (request.getRazorpayPaymentId() == null || request.getRazorpayPaymentId().isBlank()) {
            throw new RuntimeException("razorpayPaymentId is required");
        }
        if (request.getRazorpaySignature() == null || request.getRazorpaySignature().isBlank()) {
            throw new RuntimeException("razorpaySignature is required");
        }
        if (request.getCustomerName() == null || request.getCustomerName().isBlank()) {
            throw new RuntimeException("Customer name is required");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new RuntimeException("Email is required");
        }
        if (request.getAddress() == null || request.getAddress().isBlank()) {
            throw new RuntimeException("Address is required");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("Items are required");
        }
    }

    private int calculateAmountPaise(List<OrderItemRequest> items) {
        double total = 0.0;

        for (OrderItemRequest item : items) {
            if (item.getPrice() == null || item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new RuntimeException("Invalid item data");
            }
            total += item.getPrice() * item.getQuantity();
        }

        return (int) Math.round(total * 100);
    }

    private boolean verifySignature(String serverOrderId, String razorpayPaymentId, String razorpaySignature) throws Exception {
        String payload = serverOrderId + "|" + razorpayPaymentId;

        Mac sha256Hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(
                razorpayKeySecret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );
        sha256Hmac.init(secretKey);

        byte[] hash = sha256Hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        String generatedSignature = bytesToHex(hash);

        return generatedSignature.equals(razorpaySignature);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}