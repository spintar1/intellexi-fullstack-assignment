package com.intellexi.query.api;

import com.intellexi.query.model.User;
import com.intellexi.query.repo.UserRepository;
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
    private final UserRepository userRepository;
    
    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
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
			// Validate user exists in database
			Optional<User> userOpt = userRepository.findByEmail(req.getEmail());
			if (userOpt.isEmpty()) {
				logger.warn("Authentication failed - user not found: {}", req.getEmail());
				return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials - user not found"));
			}
			
			User user = userOpt.get();
			
			// Validate role matches what's in database
			if (!user.getRole().name().equals(req.getRole())) {
				logger.warn("Authentication failed - role mismatch for user: {} (requested: {}, actual: {})", 
						   req.getEmail(), req.getRole(), user.getRole());
				return ResponseEntity.status(401).body(Map.of("error", "Invalid role for user"));
			}
			
			logger.info("User authenticated successfully: {} with role: {}", req.getEmail(), user.getRole());
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
