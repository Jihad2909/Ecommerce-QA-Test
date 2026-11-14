package com.commercial.project.controller;

import com.commercial.project.exceptions.ResourceNotFoundExceptions;
import com.commercial.project.models.AuditLog;
import com.commercial.project.models.User;
import com.commercial.project.models.enums.Role;
import com.commercial.project.repositories.AuditLogRepository;
import com.commercial.project.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<User> updateUserRole(@PathVariable Long id, @RequestParam Role role, Authentication auth) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundExceptions("Utilisateur non trouvé ID: " + id));
        user.setRole(role);
        User updated = userRepository.save(user);

        auditLogRepository.save(AuditLog.builder()
                .userEmail(auth.getName())
                .action("UPDATE_USER_ROLE")
                .targetEntity("User")
                .entityId(id)
                .timestamp(LocalDateTime.now())
                .details("Changement de rôle vers " + role.name() + " pour " + user.getEmail())
                .build());

        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id, Authentication auth) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundExceptions("Utilisateur non trouvé ID: " + id));
        userRepository.delete(user);

        auditLogRepository.save(AuditLog.builder()
                .userEmail(auth.getName())
                .action("DELETE_USER")
                .targetEntity("User")
                .entityId(id)
                .timestamp(LocalDateTime.now())
                .details("Suppression du compte : " + user.getEmail())
                .build());

        return ResponseEntity.noContent().build();
    }
}