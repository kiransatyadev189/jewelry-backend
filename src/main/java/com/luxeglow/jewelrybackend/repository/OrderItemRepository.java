package com.luxeglow.jewelrybackend.repository;

import com.luxeglow.jewelrybackend.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}