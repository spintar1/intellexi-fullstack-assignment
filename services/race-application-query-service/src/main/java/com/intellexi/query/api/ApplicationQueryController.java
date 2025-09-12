package com.intellexi.query.api;

import com.intellexi.query.model.Application;
import com.intellexi.query.repo.ApplicationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/applications")
public class ApplicationQueryController {
    private final ApplicationRepository applicationRepository;

    public ApplicationQueryController(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('Applicant','Administrator')")
    public List<Application> all(Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_Administrator"));
        if (isAdmin) return applicationRepository.findAll();
        String email = String.valueOf(auth.getPrincipal());
        return applicationRepository.findByApplicantEmail(email);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('Applicant','Administrator')")
    public ResponseEntity<?> one(@PathVariable UUID id, Authentication auth) {
        return applicationRepository.findById(id).map(a -> {
            boolean isAdmin = auth.getAuthorities().stream().anyMatch(x -> x.getAuthority().equals("ROLE_Administrator"));
            if (isAdmin || String.valueOf(auth.getPrincipal()).equals(a.getApplicantEmail())) return ResponseEntity.ok(a);
            return ResponseEntity.status(403).build();
        }).orElse(ResponseEntity.notFound().build());
    }
}


