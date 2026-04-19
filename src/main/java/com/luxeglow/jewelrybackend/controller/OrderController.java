package com.luxeglow.jewelrybackend.controller;

import com.luxeglow.jewelrybackend.dto.OrderRequest;
import com.luxeglow.jewelrybackend.entity.Order;
import com.luxeglow.jewelrybackend.service.OrderService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "https://fashion-jewelry-store-frontend.vercel.app"
})
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
    public Order updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String status = body.get("status");

        if (status == null || status.isBlank()) {
            throw new RuntimeException("Status is required");
        }

        return orderService.updateOrderStatus(id, status);
    }

    @GetMapping("/track")
    public Order trackOrder(@RequestParam Long id, @RequestParam String email) {
        return orderService.trackOrder(id, email);
    }

    @GetMapping("/my")
    public List<Order> getMyOrders(Authentication authentication) {
        String email = authentication.getName();
        return orderService.getOrdersByEmail(email);
    }

    @PutMapping("/{id}/cancel")
    public Order cancelMyOrder(@PathVariable Long id, Authentication authentication) {
        String email = authentication.getName();
        return orderService.cancelOrder(id, email);
    }
}