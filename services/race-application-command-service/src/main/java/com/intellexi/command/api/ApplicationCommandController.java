package com.intellexi.command.api;

import com.intellexi.command.events.ApplicationEvents;
import com.intellexi.command.events.EventPublisher;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    public static class ApplicationRequest {
        @NotBlank private String firstName;
        @NotBlank private String lastName;
        private String club;
        @NotNull private UUID raceId;
        public ApplicationRequest() {}
        public ApplicationRequest(String firstName, String lastName, String club, UUID raceId) { this.firstName=firstName; this.lastName=lastName; this.club=club; this.raceId=raceId; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getClub() { return club; }
        public UUID getRaceId() { return raceId; }
    }

    private final EventPublisher publisher;

    public ApplicationCommandController(EventPublisher publisher) {
        this.publisher = publisher;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('Administrator','Applicant')")
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody ApplicationRequest req, Authentication auth) {
        UUID id = UUID.randomUUID();
        String email = auth == null ? null : String.valueOf(auth.getPrincipal());
        publisher.publishApplicationEvent(new ApplicationEvents.ApplicationCreated(
                id, req.getFirstName(), req.getLastName(), req.getClub(), req.getRaceId(), email
        ));
        return ResponseEntity.accepted().body(Map.of("id", id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('Administrator','Applicant')")
    public ResponseEntity<Void> delete(HttpServletRequest request, Authentication auth) {
        // Extract ID from the URL path manually
        String path = request.getRequestURI();
        String idString = path.substring(path.lastIndexOf('/') + 1);
        UUID id = UUID.fromString(idString);
        
        String email = auth == null ? null : String.valueOf(auth.getPrincipal());
        String role = auth == null ? null : auth.getAuthorities().stream().findFirst().map(a -> a.getAuthority().replace("ROLE_", "")).orElse(null);
        publisher.publishApplicationEvent(new ApplicationEvents.ApplicationDeleted(id, email, role));
        return ResponseEntity.accepted().build();
    }
}


