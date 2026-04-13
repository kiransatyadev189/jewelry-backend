package com.luxeglow.jewelrybackend.repository;

import com.luxeglow.jewelrybackend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}