package com.intellexi.command.api;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@Validated
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    public static class TokenRequest {
        @NotBlank private String email;
        @NotBlank private String role;

        public TokenRequest() {}
        public TokenRequest(String email, String role) { this.email = email; this.role = role; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
    }

	@PostMapping("/token")
	public ResponseEntity<Map<String, String>> token(@RequestBody TokenRequest req) {
		logger.info("Received token request for user: {} with role: {}", req.getEmail(), req.getRole());
		
		try {
			String secret = Optional.ofNullable(System.getenv("JWT_SECRET")).orElse("dev-shared-secret-please-change-this-is-a-very-long-secret-key-for-jwt-signing-that-is-at-least-256-bits-long");
			Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
			
			Date issuedAt = new Date();
			Date expiresAt = Date.from(Instant.now().plusSeconds(60L * 60 * 8));
			logger.debug("Creating JWT token - issued: {}, expires: {}", issuedAt, expiresAt);
			
			String token = Jwts.builder()
				.setSubject(req.getEmail())
				.claim("role", req.getRole())
				.setIssuedAt(issuedAt)
				.setExpiration(expiresAt)
				.signWith(key, SignatureAlgorithm.HS256)
				.compact();
			
			logger.info("Successfully generated JWT token for user: {} (expires in 8 hours)", req.getEmail());
			return ResponseEntity.ok(Map.of("token", token));
			
		} catch (Exception e) {
			logger.error("Failed to generate JWT token for user: {} with role: {}", req.getEmail(), req.getRole(), e);
			return ResponseEntity.internalServerError().build();
		}
	}
} 