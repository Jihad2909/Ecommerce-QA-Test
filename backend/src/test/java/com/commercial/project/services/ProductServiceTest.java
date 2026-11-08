package com.commercial.project.services;


import com.commercial.project.exceptions.BusinessRuleException;
import com.commercial.project.models.AuditLog;
import com.commercial.project.models.Product;
import com.commercial.project.repositories.AuditLogRepository;
import com.commercial.project.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private ProductService productService;

    private Product exempleProduct;

    @BeforeEach
    void setUp() {
        exempleProduct = Product.builder()
                .id(1L)
                .sku("LAPTOP-HP-01")
                .name("Hp Victus")
                .unitPrice(new BigDecimal("1200.0"))
                .stockQuantity(10)
                .minStockAlert(3)
                .build();
    }

    @Test
    @DisplayName("Régle 2: Succés de création si le SKU est unique")
    void shouldCreateProductWhenSkuIsUnique() {
        //Given
        when(productRepository.existsBySku("LAPTOP-HP-01")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(exempleProduct);

        //WHEN
        Product created = productService.createProduct(exempleProduct,"Jihad@admin.com");

        //THEN
        assertNotNull(created);
        assertEquals("LAPTOP-HP-01", created.getSku());
        verify(productRepository, times(1)).save(exempleProduct);
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("Régle 2: Exception levé si le SKU existe déja")
    void shouldThrowExceptionWhenSkuIsAlreadyExists() {
        //GIVEN
        when(productRepository.existsBySku("LAPTOP-HP-01")).thenReturn(true);

        //WHEN & THEN
        assertThrows(BusinessRuleException.class,
                ()-> productService.createProduct(exempleProduct,"jihad@admin.com"));

        verify(productRepository, never()).save(any(Product.class));
        verify(auditLogRepository, never()).save(any(AuditLog.class));
    }


}
