package com.intellexi.command.api;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.validation.constraints.NotBlank;
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
		String secret = Optional.ofNullable(System.getenv("JWT_SECRET")).orElse("dev-shared-secret-please-change-this-is-a-very-long-secret-key-for-jwt-signing-that-is-at-least-256-bits-long");
		Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		String token = Jwts.builder()
			.setSubject(req.getEmail())
			.claim("role", req.getRole())
			.setIssuedAt(new Date())
			.setExpiration(Date.from(Instant.now().plusSeconds(60L * 60 * 8)))
			.signWith(key, SignatureAlgorithm.HS256)
			.compact();
		return ResponseEntity.ok(Map.of("token", token));
	}
} 