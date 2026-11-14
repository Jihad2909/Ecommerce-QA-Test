package com.commercial.project.services;

import com.commercial.project.dto.AuthRequest;
import com.commercial.project.dto.AuthResponse;
import com.commercial.project.models.User;
import com.commercial.project.models.enums.Role;
import com.commercial.project.repositories.UserRepository;
import com.commercial.project.security.JwtUtils;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public ResponseEntity<?> register(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            return ResponseEntity.badRequest().body("Cet email est déjà utilisé.");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getRole() == null) {
            user.setRole(Role.ROLE_CLIENT);
        }
        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(savedUser);
    }

    public ResponseEntity<?> login(AuthRequest authRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword())
        );
        User user = userRepository.findByEmail(authRequest.getEmail()).orElseThrow(
                ()-> new RuntimeException("User not found.")
        );
        String jwt = jwtUtils.generateToken(user.getEmail(), user.getRole().name());
        return ResponseEntity.ok(new AuthResponse(jwt, user.getRole().name(),user.getEmail()));
    }
}
