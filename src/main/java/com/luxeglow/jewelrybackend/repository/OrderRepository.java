package com.luxeglow.jewelrybackend.repository;

import com.luxeglow.jewelrybackend.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByIdAndEmail(Long id, String email);

    List<Order> findByEmailOrderByOrderDateDesc(String email);
    Optional<Order> findByRazorpayPaymentId(String razorpayPaymentId);
}