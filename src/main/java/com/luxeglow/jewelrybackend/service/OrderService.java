package com.luxeglow.jewelrybackend.service;

import com.luxeglow.jewelrybackend.dto.OrderItemRequest;
import com.luxeglow.jewelrybackend.dto.OrderRequest;
import com.luxeglow.jewelrybackend.entity.Order;
import com.luxeglow.jewelrybackend.entity.OrderItem;
import com.luxeglow.jewelrybackend.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class OrderService {

    private static final Set<String> ALLOWED_STATUSES = Set.of(
            "Pending",
            "Confirmed",
            "Shipped",
            "Out for Delivery",
            "Delivered",
            "Cancelled"
    );

    private final OrderRepository orderRepository;
    private final EmailService emailService;

    public OrderService(OrderRepository orderRepository, EmailService emailService) {
        this.orderRepository = orderRepository;
        this.emailService = emailService;
    }

    public Order placeOrder(OrderRequest request) {
        System.out.println("placeOrder() started");

        Order order = new Order();
        order.setCustomerName(request.getCustomerName());
        order.setEmail(request.getEmail());
        order.setAddress(request.getAddress());
        order.setTotalAmount(request.getTotalAmount());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("Pending");
        order.setExpectedDeliveryDate(null);

        order.setPaymentStatus("PENDING");
        order.setPaymentMethod("COD");
        order.setRazorpayOrderId(null);
        order.setRazorpayPaymentId(null);

        if (request.getItems() != null) {
            for (OrderItemRequest itemRequest : request.getItems()) {
                OrderItem item = new OrderItem();
                item.setProductId(itemRequest.getProductId());
                item.setProductName(itemRequest.getProductName());
                item.setQuantity(itemRequest.getQuantity());
                item.setPrice(itemRequest.getPrice());
                item.setImageUrl(itemRequest.getImageUrl());
                item.setOrder(order);
                order.getItems().add(item);
            }
        }

        Order savedOrder = orderRepository.save(order);
        System.out.println("Order saved with id: " + savedOrder.getId());

        try {
            emailService.sendOrderConfirmation(savedOrder);
            System.out.println("Order confirmation email sent to: " + savedOrder.getEmail());
        } catch (Exception e) {
            System.out.println("Failed to send confirmation email: " + e.getMessage());
            e.printStackTrace();
        }

        return savedOrder;
    }

    public Order placePaidOrder(OrderRequest request, String razorpayOrderId, String razorpayPaymentId) {
        System.out.println("placePaidOrder() started");

        Order order = new Order();
        order.setCustomerName(request.getCustomerName());
        order.setEmail(request.getEmail());
        order.setAddress(request.getAddress());
        order.setTotalAmount(request.getTotalAmount());
        order.setOrderDate(LocalDateTime.now());

        order.setStatus("Confirmed");
        order.setExpectedDeliveryDate(null);
        order.setPaymentStatus("PAID");
        order.setPaymentMethod("RAZORPAY");
        order.setRazorpayOrderId(razorpayOrderId);
        order.setRazorpayPaymentId(razorpayPaymentId);

        if (request.getItems() != null) {
            for (OrderItemRequest itemRequest : request.getItems()) {
                OrderItem item = new OrderItem();
                item.setProductId(itemRequest.getProductId());
                item.setProductName(itemRequest.getProductName());
                item.setQuantity(itemRequest.getQuantity());
                item.setPrice(itemRequest.getPrice());
                item.setImageUrl(itemRequest.getImageUrl());
                item.setOrder(order);
                order.getItems().add(item);
            }
        }

        Order savedOrder = orderRepository.save(order);
        System.out.println("Paid order saved with id: " + savedOrder.getId());

        try {
            emailService.sendOrderConfirmation(savedOrder);
            System.out.println("Order confirmation email sent to: " + savedOrder.getEmail());
        } catch (Exception e) {
            System.out.println("Failed to send confirmation email: " + e.getMessage());
            e.printStackTrace();
        }

        return savedOrder;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order updateOrderStatus(Long id, String status, String expectedDeliveryDate) {
        Optional<Order> optionalOrder = orderRepository.findById(id);

        if (optionalOrder.isEmpty()) {
            throw new RuntimeException("Order not found with id: " + id);
        }

        if (status == null || status.isBlank()) {
            throw new RuntimeException("Order status cannot be empty");
        }

        String normalizedStatus = status.trim();

        if (!ALLOWED_STATUSES.contains(normalizedStatus)) {
            throw new RuntimeException("Invalid order status: " + normalizedStatus);
        }

        Order order = optionalOrder.get();
        order.setStatus(normalizedStatus);

        if (expectedDeliveryDate != null && !expectedDeliveryDate.isBlank()) {
            order.setExpectedDeliveryDate(LocalDate.parse(expectedDeliveryDate));
        } else if ("Delivered".equalsIgnoreCase(normalizedStatus)) {
            if (order.getExpectedDeliveryDate() == null) {
                order.setExpectedDeliveryDate(LocalDate.now());
            }
        }

        Order updatedOrder = orderRepository.save(order);
        System.out.println("Order status updated for id: " + updatedOrder.getId() + " to " + updatedOrder.getStatus());

        try {
            emailService.sendOrderStatusUpdate(updatedOrder);
            System.out.println("Status update email sent to: " + updatedOrder.getEmail());
        } catch (Exception e) {
            System.out.println("Failed to send status update email: " + e.getMessage());
            e.printStackTrace();
        }

        return updatedOrder;
    }

    public Order trackOrder(Long id, String email) {
        if (id == null) {
            throw new RuntimeException("Order ID is required");
        }

        if (email == null || email.isBlank()) {
            throw new RuntimeException("Email is required");
        }

        return orderRepository.findByIdAndEmail(id, email.trim())
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    public List<Order> getOrdersByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new RuntimeException("Email is required");
        }

        return orderRepository.findByEmailOrderByOrderDateDesc(email.trim());
    }

    public Order cancelOrder(Long id, String email) {
        if (id == null) {
            throw new RuntimeException("Order ID is required");
        }

        if (email == null || email.isBlank()) {
            throw new RuntimeException("Email is required");
        }

        Order order = orderRepository.findByIdAndEmail(id, email.trim())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        String currentStatus = order.getStatus() == null ? "" : order.getStatus().trim();

        if (currentStatus.equalsIgnoreCase("Shipped")
                || currentStatus.equalsIgnoreCase("Out for Delivery")
                || currentStatus.equalsIgnoreCase("Delivered")
                || currentStatus.equalsIgnoreCase("Cancelled")) {
            throw new RuntimeException("This order cannot be cancelled now");
        }

        order.setStatus("Cancelled");

        Order cancelledOrder = orderRepository.save(order);

        try {
            emailService.sendOrderStatusUpdate(cancelledOrder);
            System.out.println("Cancellation email sent to: " + cancelledOrder.getEmail());
        } catch (Exception e) {
            System.out.println("Failed to send cancellation email: " + e.getMessage());
            e.printStackTrace();
        }

        return cancelledOrder;
    }
}