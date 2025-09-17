package com.intellexi.query.api;

import com.intellexi.query.model.Application;
import com.intellexi.query.repo.ApplicationRepository;
import com.intellexi.query.repo.UserRepository;
import com.intellexi.query.dto.ApplicationWithUserDto;
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
    private final UserRepository userRepository;

    public ApplicationQueryController(ApplicationRepository applicationRepository, UserRepository userRepository) {
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('Applicant','Administrator')")
    public List<ApplicationWithUserDto> all(Authentication auth) {
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
                // Look up user by email to get userId, then find applications
                applications = userRepository.findByEmail(user)
                    .map(foundUser -> applicationRepository.findByUserId(foundUser.getId()))
                    .orElseGet(() -> {
                        logger.warn("User not found for email: {}", user);
                        return List.of();
                    });
            }
            
            // Convert to DTOs with user data
            List<ApplicationWithUserDto> applicationDtos = applications.stream()
                .map(app -> {
                    // Get user data for this application
                    return userRepository.findById(app.getUserId())
                        .map(appUser -> new ApplicationWithUserDto(
                            app.getId(),
                            app.getRaceId(),
                            app.getUserId(),
                            appUser.getFirstName(),
                            appUser.getLastName(),
                            appUser.getEmail(),
                            appUser.getClub()
                        ))
                        .orElse(new ApplicationWithUserDto(
                            app.getId(),
                            app.getRaceId(),
                            app.getUserId(),
                            "Unknown", "User", "unknown@example.com", null
                        ));
                })
                .toList();
            
            logger.info("Successfully retrieved {} applications for user: {}", applicationDtos.size(), user);
            return applicationDtos;
            
        } catch (Exception e) {
            logger.error("Failed to retrieve applications for user: {}, isAdmin: {}", user, isAdmin, e);
            throw e; // Re-throw to let Spring handle it
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('Applicant','Administrator')")
    public ResponseEntity<ApplicationWithUserDto> one(@PathVariable UUID id, Authentication auth) {
        String user = String.valueOf(auth.getPrincipal());
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(x -> x.getAuthority().equals("ROLE_Administrator"));
        
        logger.info("Received single application query request - id: {}, user: {}, isAdmin: {}", id, user, isAdmin);
        
        try {
            return applicationRepository.findById(id).<ResponseEntity<ApplicationWithUserDto>>map(a -> {
                if (isAdmin) {
                    // Create DTO with user data
                    ApplicationWithUserDto dto = userRepository.findById(a.getUserId())
                        .map(appUser -> new ApplicationWithUserDto(
                            a.getId(),
                            a.getRaceId(),
                            a.getUserId(),
                            appUser.getFirstName(),
                            appUser.getLastName(),
                            appUser.getEmail(),
                            appUser.getClub()
                        ))
                        .orElse(new ApplicationWithUserDto(
                            a.getId(), a.getRaceId(), a.getUserId(),
                            "Unknown", "User", "unknown@example.com", null
                        ));
                    
                    logger.info("Successfully retrieved application - id: {}, user: {} (admin)", id, user);
                    return ResponseEntity.ok(dto);
                } else {
                    // Check if user owns this application
                    return userRepository.findByEmail(user)
                        .<ResponseEntity<ApplicationWithUserDto>>map(foundUser -> {
                            if (foundUser.getId().equals(a.getUserId())) {
                                ApplicationWithUserDto dto = new ApplicationWithUserDto(
                                    a.getId(),
                                    a.getRaceId(),
                                    a.getUserId(),
                                    foundUser.getFirstName(),
                                    foundUser.getLastName(),
                                    foundUser.getEmail(),
                                    foundUser.getClub()
                                );
                                
                                logger.info("Successfully retrieved application - id: {}, user: {}", id, user);
                                return ResponseEntity.ok(dto);
                            } else {
                                logger.warn("Access denied for application - id: {}, requestUser: {}, ownerId: {}", 
                                           id, user, a.getUserId());
                                return ResponseEntity.<ApplicationWithUserDto>status(403).build();
                            }
                        })
                        .orElseGet(() -> {
                            logger.warn("User not found for access check - email: {}", user);
                            return ResponseEntity.<ApplicationWithUserDto>status(403).build();
                        });
                }
            }).orElseGet(() -> {
                logger.warn("Application not found - id: {}, user: {}", id, user);
                return ResponseEntity.<ApplicationWithUserDto>notFound().build();
            });
            
        } catch (Exception e) {
            logger.error("Failed to retrieve application - id: {}, user: {}", id, user, e);
            return ResponseEntity.<ApplicationWithUserDto>internalServerError().build();
        }
    }
}


