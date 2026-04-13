package com.luxeglow.jewelrybackend.repository;

import com.luxeglow.jewelrybackend.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}