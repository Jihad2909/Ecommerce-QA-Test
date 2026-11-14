package com.commercial.project.services;

import com.commercial.project.exceptions.BusinessRuleException;
import com.commercial.project.exceptions.ResourceNotFoundExceptions;
import com.commercial.project.models.AuditLog;
import com.commercial.project.models.Product;
import com.commercial.project.repositories.AuditLogRepository;
import com.commercial.project.repositories.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final AuditLogRepository auditLogRepository;

    @Transactional
    public Product createProduct(Product product, String userEmail) {

        if (productRepository.existsBySku(product.getSku())) {
            throw new BusinessRuleException("Product with id " + product.getId() + " already exists");
        }
        if (product.getStockQuantity() == null) {
            product.setStockQuantity(0);
        }
        if (product.getMinStockAlert() == null) {
            product.setMinStockAlert(2);
        }

        Product newProduct = productRepository.save(product);;

        AuditLog auditLog = AuditLog.builder()
                .userEmail(userEmail)
                .action("CREATE_PRODUCT")
                .targetEntity("Product")
                .entityId(newProduct.getId())
                .timestamp(LocalDateTime.now())
                .details("Création de produit: "+ newProduct.getSku())
                .build();
        auditLogRepository.save(auditLog);

        return newProduct;
    }

    public Page<Product> searchProduct(String name, Pageable pageable) {
        if(name == null || name.isBlank()){
            return productRepository.findAll((org.springframework.data.domain.Pageable) pageable);
        }
        return productRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    public Product getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundExceptions("Produit introuvable ID: " + id));
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Transactional 
    public Product updateProduct(long id, Product updated, String userEmail){
        Product existing = getById(id);
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setUnitPrice(updated.getUnitPrice());
        existing.setStockQuantity(updated.getStockQuantity());
        existing.setMinStockAlert(updated.getMinStockAlert());
        Product saved = productRepository.save(existing);

        auditLogRepository.save(AuditLog.builder()
                .userEmail(userEmail)
                .action("UPDATE_PRODUCT")
                .targetEntity("Product")
                .entityId(saved.getId())
                .timestamp(LocalDateTime.now())
                .details("Mise à jour produit SKU: " + saved.getSku())
                .build());
        return saved;
    }

    @Transactional
    public void deleteProduct(Long id, String userEmail) {
        Product existing = getById(id);
        productRepository.delete(existing);
        auditLogRepository.save(AuditLog.builder()
                .userEmail(userEmail)
                .action("DELETE_PRODUCT")
                .targetEntity("Product")
                .entityId(id)
                .timestamp(LocalDateTime.now())
                .details("Suppression produit SKU: " + existing.getSku())
                .build());
    }


}
