package com.aaspaas.aaspaas_backend.order.repository;

import com.aaspaas.aaspaas_backend.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository
        extends JpaRepository<OrderItem, Long> {
}