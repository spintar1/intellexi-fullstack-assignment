package com.intellexi.command.api;

import com.intellexi.command.events.EventPublisher;
import com.intellexi.command.events.RaceEvents;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/races")
@Validated
public class RaceCommandController {
    private static final Logger logger = LoggerFactory.getLogger(RaceCommandController.class);

    public static final Set<String> VALID_DISTANCES = Set.of("5k", "10k", "HalfMarathon", "Marathon");

    public static class CreateRaceRequest {
        @NotBlank private String name;
        @NotBlank private String distance;
        public CreateRaceRequest() {}
        public CreateRaceRequest(String name, String distance) { this.name = name; this.distance = distance; }
        public String getName() { return name; }
        public String getDistance() { return distance; }
    }

    public static class UpdateRaceRequest {
        private String name;
        private String distance;
        public UpdateRaceRequest() {}
        public UpdateRaceRequest(String name, String distance) { this.name = name; this.distance = distance; }
        public String getName() { return name; }
        public String getDistance() { return distance; }
    }

    private final EventPublisher publisher;

    public RaceCommandController(EventPublisher publisher) {
        this.publisher = publisher;
    }

    @PostMapping
    @PreAuthorize("hasRole('Administrator')")
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody CreateRaceRequest req) {
        logger.info("Received race creation request - name: '{}', distance: '{}'", req.getName(), req.getDistance());
        
        try {
            if (!VALID_DISTANCES.contains(req.getDistance())) {
                logger.warn("Invalid distance rejected - '{}' not in allowed distances: {}", req.getDistance(), VALID_DISTANCES);
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid distance"));
            }
            
            UUID id = UUID.randomUUID();
            logger.debug("Generated race ID: {}", id);
            
            logger.info("Publishing race created event - id: {}, name: '{}', distance: '{}'", id, req.getName(), req.getDistance());
            publisher.publishRaceEvent(new RaceEvents.RaceCreated(id, req.getName(), req.getDistance()));
            
            logger.info("Race created successfully - id: {}, name: '{}'", id, req.getName());
            return ResponseEntity.created(URI.create("/api/v1/races/" + id)).body(Map.of("id", id, "message", "Race created successfully. Note: duplicate races with the same name and distance are not allowed."));
            
        } catch (Exception e) {
            logger.error("Failed to create race - name: '{}', distance: '{}'", req.getName(), req.getDistance(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to create race. This might be due to a duplicate race with the same name and distance."));
        }
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('Administrator')")
    public ResponseEntity<Void> update(HttpServletRequest request, @RequestBody UpdateRaceRequest req) {
        // Extract ID from the URL path manually
        String path = request.getRequestURI();
        String idString = path.substring(path.lastIndexOf('/') + 1);
        
        try {
            UUID id = UUID.fromString(idString);
            String name = req.getName();
            String distance = req.getDistance();
            
            logger.info("Received race update request - id: {}, name: '{}', distance: '{}'", id, name, distance);
            
            if (distance != null && !VALID_DISTANCES.contains(distance)) {
                logger.warn("Invalid distance rejected for race update - id: {}, distance: '{}' not in allowed distances: {}", 
                           id, distance, VALID_DISTANCES);
                return ResponseEntity.badRequest().build();
            }
            
            logger.info("Publishing race updated event - id: {}, name: '{}', distance: '{}'", id, name, distance);
            publisher.publishRaceEvent(new RaceEvents.RaceUpdated(id, name, distance));
            
            logger.info("Race updated successfully - id: {}", id);
            return ResponseEntity.ok().build();
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid UUID format for race update: '{}' - path: {}", idString, path, e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Failed to update race - id: '{}', name: '{}', distance: '{}'", 
                        idString, req.getName(), req.getDistance(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('Administrator')")
    public ResponseEntity<Void> delete(HttpServletRequest request, Authentication auth) {
        // Extract ID from the URL path manually
        String path = request.getRequestURI();
        String idString = path.substring(path.lastIndexOf('/') + 1);
        
        try {
            UUID id = UUID.fromString(idString);
            String adminUser = auth != null ? String.valueOf(auth.getPrincipal()) : "unknown";
            
            logger.info("Received race delete request - id: {}, admin: {}", id, adminUser);
            if (auth != null) {
                logger.debug("Delete request authentication details - user: {}, authorities: {}", 
                           auth.getPrincipal(), auth.getAuthorities());
            }
            
            logger.info("Publishing race deleted event - id: {}, admin: {}", id, adminUser);
            publisher.publishRaceEvent(new RaceEvents.RaceDeleted(id));
            
            logger.info("Race delete request processed successfully - id: {}, admin: {}", id, adminUser);
            return ResponseEntity.noContent().build();
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid UUID format for race deletion: '{}' - path: {}", idString, path, e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Failed to process race deletion - id: '{}', admin: {}", idString, 
                        auth != null ? auth.getPrincipal() : "unknown", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/debug")
    @PreAuthorize("hasRole('Administrator')")
    public ResponseEntity<Map<String, Object>> debug(Authentication auth) {
        if (auth == null) {
            return ResponseEntity.ok(Map.of("auth", "null"));
        }
        return ResponseEntity.ok(Map.of(
            "principal", auth.getPrincipal(),
            "authorities", auth.getAuthorities().toString(),
            "authenticated", auth.isAuthenticated()
        ));
    }
}


