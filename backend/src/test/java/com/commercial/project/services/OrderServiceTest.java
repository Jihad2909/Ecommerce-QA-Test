package com.commercial.project.services;

import com.commercial.project.exceptions.BusinessRuleException;
import com.commercial.project.models.*;
import com.commercial.project.models.enums.OrderStatus;
import com.commercial.project.models.enums.Role;
import com.commercial.project.repositories.AuditLogRepository;
import com.commercial.project.repositories.OrderRepository;
import com.commercial.project.repositories.ProductRepository;
import com.commercial.project.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private OrderService orderService;

    private User client;
    private Product product;

    @BeforeEach
    void setUp() {
        client = User.builder()
                .id(1L)
                .email("jihad@admin.com")
                .fullName("Jihad Mazrani")
                .role(Role.ROLE_CLIENT)
                .build();
        product = Product.builder()
                .id(10L)
                .sku("SCREEN-LG-27")
                .name("LG UltraGear 27")
                .unitPrice(new BigDecimal("300.0"))
                .stockQuantity(5)
                .minStockAlert(2)
                .build();
    }
    //Régle 1 et 5 : Création de commande
    @Test
    @DisplayName("Régle 5: Calcul exacte de total coté serveur (prix unitaire fixe)")
    void shouldCalculateCorrectServerSideTotalOnCreateOrder(){
        //GIVEN
        Map<Long, Integer> itemsOrder = new HashMap<>();
        itemsOrder.put(10L, 2);

        //WHEN
        when(userRepository.findByEmail("jihad@admin.com")).thenReturn(Optional.of(client));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        Order createdOrder = orderService.createOrder("jihad@admin.com",itemsOrder);

        //THEN
        assertNotNull(createdOrder);
        assertEquals(OrderStatus.SUBMITTED, createdOrder.getStatus());
        assertEquals(0, new BigDecimal("600").compareTo(createdOrder.getTotalAmount()));
        assertEquals(1, createdOrder.getItems().size());
        assertEquals(0, new BigDecimal("300").compareTo(createdOrder.getItems().get(0).getFrozenUnitPrice()));
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));

    }

    @Test
    @DisplayName("Regle 1: échec si la quantite demandée dépasse le stock disponible")
    void shouldThrowExceptionWhenStockIsInsufficient(){
        //GIVEN
        Map<Long, Integer> itemsOrder = Collections.singletonMap(10L, 6);

        //WHEN
        when(userRepository.findByEmail("jihad@admin.com")).thenReturn(Optional.of(client));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        assertThrows(BusinessRuleException.class, () ->
                orderService.createOrder("jihad@admin.com", itemsOrder)
        );

        //THEN
        verify(orderRepository, never()).save(any(Order.class));

    }

    //Régle 3 & WorkFlow : Confirmation er Alerts
    @Test
    @DisplayName("Régle 3: décrimentation de stock et alert seuil critique")
    void shouldConfirmOrderDecrimentationAndTriggerLowStockAlert(){
        //GIVEN
        OrderItem item = OrderItem.builder()
                .product(product)
                .Quantity(4)
                .build();
        Order order = Order.builder()
                .id(100L)
                .status(OrderStatus.SUBMITTED)
                .build();
        order.addItem(item);

        //WHEN
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        Order confirmOrder = orderService.confirmOrder(100L,"jihad@manager.com");

        //THEN
        assertEquals(OrderStatus.CONFIRMED, confirmOrder.getStatus());
        assertEquals(1, product.getStockQuantity());

        verify(productRepository, times(1)).save(any(Product.class));
        verify(auditLogRepository, times(2)).save(any(AuditLog.class));
    }

    //Régle 4: Annulation Succeded
    @Test
    @DisplayName("Régle 4: Succes d'annulation si la commande a l'etat SUBMITTED")
    void shouldCancelOrderSuccessfullyWhenStatusIsSubmitted(){
        //GIVEN
        Order order = Order.builder()
                .id(200L)
                .status(OrderStatus.SUBMITTED)
                .build();

        when(orderRepository.findById(200L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        //WHEN
        Order orderCancelled = orderService.cancelOrder(200L,"jihad@manager.com");

        //THEN
        assertEquals(OrderStatus.CANCELLED, orderCancelled.getStatus());
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));

    }

    //Régle 4: échec D'annulation
    @Test
    @DisplayName("Régle 4: échec d'annulation si la commande est déja Confirmed ou Delivred")
    void shouldThrowExceptionWhenCanncellingAlreadyConfirmedOrder(){
        //GIVEN
        Order order= Order.builder()
                .id(300L)
                .status(OrderStatus.CONFIRMED)
                .build();

        when(orderRepository.findById(300L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        //WHEN
        assertThrows(BusinessRuleException.class, () ->
                orderService.cancelOrder(300L,"jihad@manager.com")
        );

        //THEN
        verify(orderRepository, never()).save(any(Order.class));

    }

}
