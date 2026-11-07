package com.commercial.project.repositories;

import com.commercial.project.models.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.util.Optional;

@Repository

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);
    boolean existsBySku(String sku);
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
