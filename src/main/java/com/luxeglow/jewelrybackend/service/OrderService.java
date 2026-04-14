package com.luxeglow.jewelrybackend.service;

import com.luxeglow.jewelrybackend.dto.OrderItemRequest;
import com.luxeglow.jewelrybackend.dto.OrderRequest;
import com.luxeglow.jewelrybackend.entity.Order;
import com.luxeglow.jewelrybackend.entity.OrderItem;
import com.luxeglow.jewelrybackend.repository.OrderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

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

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Order not found with id: " + orderId
                ));

        String currentStatus = order.getStatus();

        if (status == null || status.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Status is required"
            );
        }

        if (currentStatus != null && currentStatus.equalsIgnoreCase(status)) {
            return order;
        }

        if ("Delivered".equalsIgnoreCase(currentStatus)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Delivered order cannot be modified"
            );
        }

        if ("Cancelled".equalsIgnoreCase(currentStatus)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cancelled order cannot be modified"
            );
        }

        if ("Cancelled".equalsIgnoreCase(status)
                && "Shipped".equalsIgnoreCase(currentStatus)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot cancel order after it is shipped"
            );
        }

        order.setStatus(status);
        return orderRepository.save(order);
    }
}