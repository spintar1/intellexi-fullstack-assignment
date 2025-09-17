package com.intellexi.command.api;

import com.intellexi.command.events.ApplicationEvents;
import com.intellexi.command.events.EventPublisher;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/applications")
@Validated
public class ApplicationCommandController {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationCommandController.class);

    public static class ApplicationRequest {
        @NotNull private UUID raceId;
        public ApplicationRequest() {}
        public ApplicationRequest(UUID raceId) { this.raceId=raceId; }
        public UUID getRaceId() { return raceId; }
    }

    private final EventPublisher publisher;

    public ApplicationCommandController(EventPublisher publisher) {
        this.publisher = publisher;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('Administrator','Applicant')")
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody ApplicationRequest req, Authentication auth) {
        String email = auth == null ? null : String.valueOf(auth.getPrincipal());
        logger.info("Received application creation request - user: {}, raceId: {}", email, req.getRaceId());
        
        try {
            UUID id = UUID.randomUUID();
            logger.debug("Generated application ID: {}", id);
            
            logger.info("Publishing application created event - id: {}, user: {}, raceId: {}", id, email, req.getRaceId());
            publisher.publishApplicationEvent(new ApplicationEvents.ApplicationCreated(
                    id, req.getRaceId(), email
            ));
            
            logger.info("Application created successfully - id: {}, user: {}", id, email);
            return ResponseEntity.accepted().body(Map.of("id", id));
            
        } catch (Exception e) {
            logger.error("Failed to create application for user: {} - raceId: {}", email, req.getRaceId(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('Administrator','Applicant')")
    public ResponseEntity<Void> delete(HttpServletRequest request, Authentication auth) {
        // Extract ID from the URL path manually
        String path = request.getRequestURI();
        String idString = path.substring(path.lastIndexOf('/') + 1);
        
        try {
            UUID id = UUID.fromString(idString);
            String email = auth == null ? null : String.valueOf(auth.getPrincipal());
            String role = auth == null ? null : auth.getAuthorities().stream().findFirst().map(a -> a.getAuthority().replace("ROLE_", "")).orElse(null);
            
            logger.info("Received application delete request - id: {}, user: {}, role: {}", id, email, role);
            
            logger.info("Publishing application deleted event - id: {}, user: {}, role: {}", id, email, role);
            publisher.publishApplicationEvent(new ApplicationEvents.ApplicationDeleted(id, email, role));
            
            logger.info("Application delete request processed successfully - id: {}, user: {}", id, email);
            return ResponseEntity.accepted().build();
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid UUID format for application deletion: '{}' - path: {}", idString, path, e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Failed to process application deletion - id: '{}', user: {}", idString, 
                        auth != null ? auth.getPrincipal() : "unknown", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}


