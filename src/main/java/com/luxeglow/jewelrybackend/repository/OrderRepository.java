package com.luxeglow.jewelrybackend.repository;

import com.luxeglow.jewelrybackend.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByIdAndEmail(Long id, String email);

}