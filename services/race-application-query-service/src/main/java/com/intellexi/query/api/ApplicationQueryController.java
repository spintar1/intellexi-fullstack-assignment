package com.intellexi.query.api;

import com.intellexi.query.model.Application;
import com.intellexi.query.repo.ApplicationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/applications")
public class ApplicationQueryController {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationQueryController.class);
    private final ApplicationRepository applicationRepository;

    public ApplicationQueryController(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('Applicant','Administrator')")
    public List<Application> all(Authentication auth) {
        String user = String.valueOf(auth.getPrincipal());
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_Administrator"));
        
        logger.info("Received applications query request - user: {}, isAdmin: {}", user, isAdmin);
        
        try {
            List<Application> applications;
            if (isAdmin) {
                logger.debug("Admin user - fetching all applications");
                applications = applicationRepository.findAll();
            } else {
                logger.debug("Regular user - fetching applications for email: {}", user);
                applications = applicationRepository.findByApplicantEmail(user);
            }
            
            logger.info("Successfully retrieved {} applications for user: {}", applications.size(), user);
            return applications;
            
        } catch (Exception e) {
            logger.error("Failed to retrieve applications for user: {}, isAdmin: {}", user, isAdmin, e);
            throw e; // Re-throw to let Spring handle it
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('Applicant','Administrator')")
    public ResponseEntity<?> one(@PathVariable UUID id, Authentication auth) {
        String user = String.valueOf(auth.getPrincipal());
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(x -> x.getAuthority().equals("ROLE_Administrator"));
        
        logger.info("Received single application query request - id: {}, user: {}, isAdmin: {}", id, user, isAdmin);
        
        try {
            return applicationRepository.findById(id).map(a -> {
                if (isAdmin || user.equals(a.getApplicantEmail())) {
                    logger.info("Successfully retrieved application - id: {}, user: {}", id, user);
                    return ResponseEntity.ok(a);
                } else {
                    logger.warn("Access denied for application - id: {}, requestUser: {}, ownerEmail: {}", 
                               id, user, a.getApplicantEmail());
                    return ResponseEntity.status(403).build();
                }
            }).orElseGet(() -> {
                logger.warn("Application not found - id: {}, user: {}", id, user);
                return ResponseEntity.notFound().build();
            });
            
        } catch (Exception e) {
            logger.error("Failed to retrieve application - id: {}, user: {}", id, user, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}


