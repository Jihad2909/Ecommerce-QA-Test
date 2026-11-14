package com.commercial.project.controller;

import com.commercial.project.dto.DashboardStats;
import com.commercial.project.models.AuditLog;
import com.commercial.project.models.Order;
import com.commercial.project.models.Product;
import com.commercial.project.models.enums.OrderStatus;
import com.commercial.project.services.AuditService;
import com.commercial.project.services.OrderService;
import com.commercial.project.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DashboardController {

    private final OrderService orderService;
    private final ProductService productService;
    private final AuditService auditService;

    @GetMapping
    public ResponseEntity<DashboardStats> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/audit-logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditLog>> auditOrders() {
        return ResponseEntity.ok(auditService.getAuditLogs());
    }

    @GetMapping("/orders")
    public ResponseEntity<List<Order>> getAllOrdersStatus(@RequestParam(required = false) OrderStatus status) {
        return ResponseEntity.ok(orderService.getAllOrdersWithStatus(status));
    }
}
