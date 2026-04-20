package com.luxeglow.jewelrybackend.controller;

import com.luxeglow.jewelrybackend.dto.CreatePaymentOrderRequest;
import com.luxeglow.jewelrybackend.dto.CreatePaymentOrderResponse;
import com.luxeglow.jewelrybackend.dto.VerifyPaymentRequest;
import com.luxeglow.jewelrybackend.entity.Order;
import com.luxeglow.jewelrybackend.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "https://fashion-jewelry-store-frontend.vercel.app"
})
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create-order")
    public ResponseEntity<CreatePaymentOrderResponse> createOrder(
            @RequestBody CreatePaymentOrderRequest request
    ) throws Exception {
        return ResponseEntity.ok(paymentService.createRazorpayOrder(request));
    }

    @PostMapping("/verify")
    public ResponseEntity<Order> verifyPayment(
            @RequestBody VerifyPaymentRequest request
    ) throws Exception {
        return ResponseEntity.ok(paymentService.verifyAndCreateOrder(request));
    }
}