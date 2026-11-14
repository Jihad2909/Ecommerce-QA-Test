package com.commercial.project.integrations;

import com.commercial.project.models.User;
import com.commercial.project.models.enums.Role;
import com.commercial.project.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        if (userRepository.findByEmail("admin@test.com").isEmpty()) {
            User admin = new User();
            admin.setFullName("Admin Test");
            admin.setEmail("admin@test.com");
            admin.setPassword(passwordEncoder.encode("Password123!"));
            admin.setRole(Role.ROLE_ADMIN);
            userRepository.save(admin);
        }
    }

    @Test
    void shouldAllowPublicReadOnProducts() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectUnauthenticatedWriteOnProducts() throws Exception {
        String productPayload = objectMapper.writeValueAsString(Map.of(
                "name", "Clavier Mécanique",
                "sku", "CLAV-INT-001",
                "description", "Clavier mécanique rétroéclairé",
                "unitPrice", 150.00,
                "stockQuantity", 20,
                "minStockAlert", 5
        ));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productPayload))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectInvalidJwtTokenOnProtectedEndpoint() throws Exception {
        String productPayload = objectMapper.writeValueAsString(Map.of(
                "name", "Clavier Mécanique",
                "sku", "CLAV-INT-001",
                "description", "Clavier mécanique rétroéclairé",
                "unitPrice", 150.00,
                "stockQuantity", 20,
                "minStockAlert", 5
        ));

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer faux.token.invalide")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productPayload))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAuthenticateCreateProductAndCreateOrderSuccessfully() throws Exception {
        // 1. Login
        String loginPayload = objectMapper.writeValueAsString(Map.of(
                "email", "admin@test.com",
                "password", "Password123!"
        ));

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("token").asText();

        // 2. Création d'un produit authentifié
        String productPayload = objectMapper.writeValueAsString(Map.of(
                "name", "Clavier Mécanique",
                "sku", "CLAV-INT-001",
                "description", "Clavier mécanique rétroéclairé",
                "unitPrice", 150.00,
                "stockQuantity", 20,
                "minStockAlert", 5
        ));

        MvcResult productResult = mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("CLAV-INT-001"))
                .andReturn();

        Long productId = objectMapper.readTree(productResult.getResponse().getContentAsString())
                .get("id").asLong();
        Long clientId = userRepository.findByEmail("admin@test.com").orElseThrow().getId();

        // 3. Création d'une commande authentifiée (l'email vient du JWT)
        String orderPayload = objectMapper.writeValueAsString(Map.of(
                "items", Map.of(productId, 2)
        ));

        MvcResult orderResult = mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderPayload))
                .andExpect(status().isOk())
                .andReturn();

        Long orderId = objectMapper.readTree(orderResult.getResponse().getContentAsString())
                .get("id").asLong();

        // 4. Confirmation et livraison de la commande (couvre @PreAuthorize ADMIN + OrderController)
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/orders/" + orderId + "/confirm")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/orders/" + orderId + "/deliver")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/orders/orders")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}