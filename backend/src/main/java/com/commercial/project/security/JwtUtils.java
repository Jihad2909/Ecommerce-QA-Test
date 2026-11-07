package com.commercial.project.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtils {

    @Value("${JWT_SECRET}")
    private String jwtSecret;

    private final long jwtExpirations = 86400000; //24h

    // 1) getting signing key
    public SecretKey getSigningKey(){
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    // 2) generate token with email and role for 24h before expirations
    public String generateToken(String email, String role){
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirations ))
                .signWith(getSigningKey())
                .compact();
    }

    // 3) getting email from generated Token
    public String getEmailFromToken(String token){
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // 4) Validate token
    public boolean validateToken(String token){
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        }catch (JwtException | IllegalArgumentException e){
            return false;
        }
    }




}
