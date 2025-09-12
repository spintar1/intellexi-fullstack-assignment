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
        if (!VALID_DISTANCES.contains(req.getDistance())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid distance"));
        }
        UUID id = UUID.randomUUID();
        publisher.publishRaceEvent(new RaceEvents.RaceCreated(id, req.getName(), req.getDistance()));
        return ResponseEntity.created(URI.create("/api/v1/races/" + id)).body(Map.of("id", id));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('Administrator')")
    public ResponseEntity<Void> update(HttpServletRequest request, @RequestBody UpdateRaceRequest req) {
        // Extract ID from the URL path manually
        String path = request.getRequestURI();
        String idString = path.substring(path.lastIndexOf('/') + 1);
        UUID id = UUID.fromString(idString);
        String name = req.getName();
        String distance = req.getDistance();
        if (distance != null && !VALID_DISTANCES.contains(distance)) {
            return ResponseEntity.badRequest().build();
        }
        publisher.publishRaceEvent(new RaceEvents.RaceUpdated(id, name, distance));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('Administrator')")
    public ResponseEntity<Void> delete(HttpServletRequest request, Authentication auth) {
        // Extract ID from the URL path manually
        String path = request.getRequestURI();
        String idString = path.substring(path.lastIndexOf('/') + 1);
        UUID id = UUID.fromString(idString);
        
        logger.info("DELETE race request - ID: {}, Auth: {}", id, auth != null ? "present" : "null");
        if (auth != null) {
            logger.info("User: {}, Authorities: {}", auth.getPrincipal(), auth.getAuthorities());
        }
        publisher.publishRaceEvent(new RaceEvents.RaceDeleted(id));
        return ResponseEntity.noContent().build();
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


