package com.commercial.project.services;

import com.commercial.project.dto.DashboardStats;
import com.commercial.project.exceptions.BusinessRuleException;
import com.commercial.project.exceptions.ResourceNotFoundExceptions;
import com.commercial.project.models.*;
import com.commercial.project.models.enums.OrderStatus;
import com.commercial.project.repositories.AuditLogRepository;
import com.commercial.project.repositories.OrderRepository;
import com.commercial.project.repositories.ProductRepository;
import com.commercial.project.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.ast.Or;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository  userRepository;
    private final AuditLogRepository auditLogRepository;

    public DashboardStats getAllOrders() {

        List<Order> orders =  orderRepository.findAll();
        List<Product> products =  productRepository.findAll();

        long pending = orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.SUBMITTED)
                .count();
        long lowStockProductCounts = products.stream()
                .filter(p -> p.getStockQuantity() <= p.getMinStockAlert())
                .count();
        BigDecimal totalRevenue = orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.CONFIRMED || o.getStatus() == OrderStatus.DELIVERED)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return DashboardStats.builder()
                .pendingOrders(pending)
                .totalOrders(orders.size())
                .totalRevenue(totalRevenue)
                .lowStockProductsCounts(lowStockProductCounts)
                .build();

    }

    @Transactional
    public Order createOrder(String clientEmail, Map<Long, Integer> itemsOrder) {

        User client = userRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new RuntimeException("Client avec ce email " + clientEmail + " est Introuvable"));

        if (itemsOrder == null || itemsOrder.isEmpty()) {
            throw new RuntimeException("Une commande ne doit pas etre vide");
        }

        Order order = Order.builder()
                .orderNumber("CMD-" + UUID.randomUUID().toString())
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.SUBMITTED)
                .client(client)
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        for(Map.Entry<Long,Integer> entry : itemsOrder.entrySet()) {
            Long productId = entry.getKey();
            Integer quantity = entry.getValue();
            if (quantity <= 0){
                throw new RuntimeException("La quantité doit etre positive");
            }
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product avec id " + productId+" Introuvable !"));

            //Verification de stock
            if (product.getStockQuantity() < quantity) {
                throw new BusinessRuleException(String.format("Stock insuffisant pour %s", product.getName()));
            }
            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .Quantity(quantity)
                    .frozenUnitPrice(product.getUnitPrice())
                    .build();
            order.addItem(orderItem);

            BigDecimal amount = product.getUnitPrice().multiply(BigDecimal.valueOf(quantity));
            totalAmount = totalAmount.add(amount);
        }
        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);

        logAudit(clientEmail, "CREATE_ORDER", "ORDER", savedOrder.getId(),"Commande crée avec "+itemsOrder.size()+" article. Total"+totalAmount);

        return savedOrder;
    }

    private void logAudit(String user, String action, String entity, Long entityId, String details) {
        AuditLog log = AuditLog.builder()
                .userEmail(user)
                .action(action)
                .targetEntity(entity)
                .entityId(entityId)
                .timestamp(LocalDateTime.now())
                .details(details)
                .build();
        auditLogRepository.save(log);
    }

    @Transactional
    public Order confirmOrder(Long orderId, String managerEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(()-> new RuntimeException("Order avec id " + orderId + " Introuvable"));

        if(order.getStatus() != OrderStatus.SUBMITTED) {
            throw new BusinessRuleException("Seule les commandes avec status SUBMITTED peut étre confirmer!");
        }

        for (OrderItem orderItem : order.getItems()) {
            Product product = orderItem.getProduct();

            //Verification de au moment de confirmation
            if(product.getStockQuantity() < orderItem.getQuantity()) {
               throw new BusinessRuleException("Rupture de stock servenue pour :"+product.getName());
            }

            int newStock = product.getStockQuantity() - orderItem.getQuantity();
            product.setStockQuantity(newStock);
            productRepository.save(product);

            //Alert si la seuil de low stock atteint
            if (newStock <= product.getMinStockAlert()) {
                logAudit("SYSTEM","ALERT_LOW_STOCK","Product", product.getId(),
                String.format("ATTENTION : Stock critique pour %s, Reste: %d, Seuil:",
                        product.getName(),newStock,product.getMinStockAlert())
                );
            }
        }
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);

        logAudit(managerEmail,"CONFIRM_ORDER","ORDER",order.getId(),
                String.format("Commande validé et stock décrimenter")
        );
        return order;
    }

    @Transactional
    public Order cancelOrder(Long orderId, String managerEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order avec id " + orderId + " Introuvable"));

        if (order.getStatus() != OrderStatus.SUBMITTED) {
            throw new BusinessRuleException("Annulation Impossible: la commande est dans l'etat "+order.getStatus());
        }
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        logAudit(managerEmail,"CANCEL_ORDER", "ORDER", order.getId(),
                "Commande annulée avec succés."
        );
        return order;
    }

    @Transactional
    public Order deliverOrder(Long orderId, String managerEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundExceptions("Commande non trouvée ID: " + orderId));

        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new BusinessRuleException("Seule une commande CONFIRMED peut passer à DELIVERED.");
        }

        order.setStatus(OrderStatus.DELIVERED);
        Order updated = orderRepository.save(order);
        logAudit(managerEmail, "DELIVER_ORDER", "Order", updated.getId(), "Commande marquée comme livrée.");
        return updated;
    }

    public List<Order> getAllOrdersWithStatus (OrderStatus status) {
        List<Order> orders = orderRepository.findAll(Sort.by(Sort.Direction.DESC, "orderDate"));
        if (status != null) {
            orders = orders.stream()
                    .filter(o -> o.getStatus() == status)
                    .toList();
        }
        return orders;
    }
}
