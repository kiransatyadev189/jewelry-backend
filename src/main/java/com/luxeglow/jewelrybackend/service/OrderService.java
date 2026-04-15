package com.luxeglow.jewelrybackend.service;

import com.luxeglow.jewelrybackend.dto.OrderItemRequest;
import com.luxeglow.jewelrybackend.dto.OrderRequest;
import com.luxeglow.jewelrybackend.entity.Order;
import com.luxeglow.jewelrybackend.entity.OrderItem;
import com.luxeglow.jewelrybackend.repository.OrderRepository;
import org.springframework.stereotype.Service;

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

        if (request.getItems() != null) {
            for (OrderItemRequest itemRequest : request.getItems()) {
                OrderItem item = new OrderItem();
                item.setProductName(itemRequest.getProductName());
                item.setQuantity(itemRequest.getQuantity());
                item.setPrice(itemRequest.getPrice());
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

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order updateOrderStatus(Long id, String status) {
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
}