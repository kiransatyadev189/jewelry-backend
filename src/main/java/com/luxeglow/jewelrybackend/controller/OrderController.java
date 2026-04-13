package com.luxeglow.jewelrybackend.controller;

import com.luxeglow.jewelrybackend.dto.OrderRequest;
import com.luxeglow.jewelrybackend.entity.Order;
import com.luxeglow.jewelrybackend.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:5173")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public Order placeOrder(@RequestBody OrderRequest request) {
        return orderService.placeOrder(request);
    }

    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    @PutMapping("/{id}/status")
    public Order updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        return orderService.updateOrderStatus(id, request.get("status"));
    }
}