package com.commercial.project.controller;

import com.commercial.project.dto.OrderCreateRequest;
import com.commercial.project.models.Order;
import com.commercial.project.models.enums.OrderStatus;
import com.commercial.project.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody OrderCreateRequest request, Authentication auth) {
        Order order = orderService.createOrder(auth.getName(), request.getItems());
        return ResponseEntity.ok(order);
    }

    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE')")
    public ResponseEntity<Order> confirmOrder(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(orderService.confirmOrder(id, auth.getName()));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Order> cancelOrder(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(orderService.cancelOrder(id, auth.getName()));
    }

    @PatchMapping("/{id}/deliver")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE')")
    public ResponseEntity<Order> deliverOrder(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(orderService.deliverOrder(id, auth.getName()));
    }

    @GetMapping("/orders")
    public ResponseEntity<List<Order>> getAllOrders(@RequestParam(required = false)OrderStatus status) {
        return ResponseEntity.ok(orderService.getAllOrdersWithStatus(status));
    }
}