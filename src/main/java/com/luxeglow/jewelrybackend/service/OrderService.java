package com.luxeglow.jewelrybackend.service;

import com.luxeglow.jewelrybackend.dto.OrderItemRequest;
import com.luxeglow.jewelrybackend.dto.OrderRequest;
import com.luxeglow.jewelrybackend.entity.Order;
import com.luxeglow.jewelrybackend.entity.OrderItem;
import com.luxeglow.jewelrybackend.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    // PLACE ORDER
    public Order placeOrder(OrderRequest request) {
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
                item.setProductId(itemRequest.getProductId());
                item.setProductName(itemRequest.getProductName());
                item.setPrice(itemRequest.getPrice());
                item.setQuantity(itemRequest.getQuantity());
                item.setOrder(order);

                order.getItems().add(item);
            }
        }

        return orderRepository.save(order);
    }

    // GET ALL ORDERS
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // ✅ UPDATE STATUS (WITH CANCEL RULES)
    public Order updateOrderStatus(Long orderId, String status) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        String currentStatus = order.getStatus();

        // 🚫 RULE 1: Cannot cancel after shipped or delivered
        if ("Cancelled".equalsIgnoreCase(status)) {
            if ("Shipped".equalsIgnoreCase(currentStatus) ||
                "Delivered".equalsIgnoreCase(currentStatus)) {
                throw new RuntimeException("Cannot cancel order after it is shipped or delivered");
            }
        }

        // 🚫 RULE 2: Cannot change status after Delivered
        if ("Delivered".equalsIgnoreCase(currentStatus)) {
            throw new RuntimeException("Delivered order cannot be modified");
        }

        // 🚫 RULE 3: Cannot update if already Cancelled
        if ("Cancelled".equalsIgnoreCase(currentStatus)) {
            throw new RuntimeException("Cancelled order cannot be modified");
        }

        // ✅ Update status
        order.setStatus(status);

        return orderRepository.save(order);
    }
}