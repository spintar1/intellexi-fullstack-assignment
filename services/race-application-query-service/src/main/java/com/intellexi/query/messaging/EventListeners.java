package com.intellexi.query.messaging;

import com.intellexi.query.model.Application;
import com.intellexi.query.model.Race;
import com.intellexi.query.repo.ApplicationRepository;
import com.intellexi.query.repo.RaceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class EventListeners {
    private static final Logger logger = LoggerFactory.getLogger(EventListeners.class);
    private final RaceRepository raceRepository;
    private final ApplicationRepository applicationRepository;

    public EventListeners(RaceRepository raceRepository, ApplicationRepository applicationRepository) {
        this.raceRepository = raceRepository;
        this.applicationRepository = applicationRepository;
    }

    @RabbitListener(queues = "${app.rabbit.queues.races}")
    public void onRaceEvent(@Payload Map<String, Object> payload) {
        logger.info("Received race event - payload keys: {}", payload.keySet());
        logger.debug("Full race event payload: {}", payload);
        
        try {
            // Simple payload mapper matching command service events
            if (payload.get("id") != null && payload.get("name") != null && payload.get("distance") != null) {
                // RaceCreated
                UUID raceId = UUID.fromString(payload.get("id").toString());
                String name = payload.get("name").toString();
                String distance = payload.get("distance").toString();
                
                logger.info("Processing race created event - id: {}, name: '{}', distance: '{}'", raceId, name, distance);
                Race race = new Race(raceId, name, distance);
                raceRepository.save(race);
                logger.info("Successfully created race - id: {}, name: '{}'", raceId, name);
                
            } else if (payload.get("id") != null && (payload.get("name") != null || payload.get("distance") != null)) {
                // RaceUpdated (partial)
                UUID id = UUID.fromString(payload.get("id").toString());
                String newName = payload.get("name") != null ? payload.get("name").toString() : null;
                String newDistance = payload.get("distance") != null ? payload.get("distance").toString() : null;
                
                logger.info("Processing race updated event - id: {}, newName: '{}', newDistance: '{}'", id, newName, newDistance);
                raceRepository.findById(id).ifPresentOrElse(existing -> {
                    String oldName = existing.getName();
                    String oldDistance = existing.getDistance();
                    
                    if (newName != null) existing.setName(newName);
                    if (newDistance != null) existing.setDistance(newDistance);
                    raceRepository.save(existing);
                    
                    logger.info("Successfully updated race - id: {}, name: '{}' -> '{}', distance: '{}' -> '{}'", 
                               id, oldName, existing.getName(), oldDistance, existing.getDistance());
                }, () -> {
                    logger.warn("Race not found for update - id: {}", id);
                });
                
            } else if (payload.get("id") != null && payload.size() == 1) {
                // RaceDeleted
                UUID raceId = UUID.fromString(payload.get("id").toString());
                logger.info("Processing race deleted event - id: {}", raceId);
                raceRepository.deleteById(raceId);
                logger.info("Successfully deleted race - id: {}", raceId);
                
            } else {
                logger.warn("Unknown race event format - payload: {}", payload);
            }
            
        } catch (Exception e) {
            logger.error("Failed to process race event - payload: {}", payload, e);
        }
    }

    @RabbitListener(queues = "${app.rabbit.queues.applications}")
    public void onApplicationEvent(@Payload Map<String, Object> payload) {
        logger.info("Received application event - payload keys: {}", payload.keySet());
        logger.debug("Full application event payload: {}", payload);
        
        try {
            if (payload.get("firstName") != null) {
                // ApplicationCreated
                UUID applicationId = UUID.fromString(payload.get("id").toString());
                String firstName = payload.get("firstName").toString();
                String lastName = payload.get("lastName").toString();
                String club = (String) payload.get("club");
                UUID raceId = UUID.fromString(payload.get("raceId").toString());
                String applicantEmail = (String) payload.get("applicantEmail");
                
                logger.info("Processing application created event - id: {}, applicant: {} {}, email: {}, raceId: {}, club: '{}'", 
                           applicationId, firstName, lastName, applicantEmail, raceId, club);
                
                Application a = new Application(applicationId, firstName, lastName, club, raceId, applicantEmail);
                applicationRepository.save(a);
                
                logger.info("Successfully created application - id: {}, applicant: {} {}, email: {}", 
                           applicationId, firstName, lastName, applicantEmail);
                
            } else if (payload.get("id") != null && payload.size() >= 1 && payload.get("firstName") == null) {
                // ApplicationDeleted — enforce that either admin initiated, or applicant deleting own app
                UUID id = UUID.fromString(payload.get("id").toString());
                String initiatorRole = (String) payload.getOrDefault("initiatorRole", "Applicant");
                String initiatorEmail = (String) payload.get("applicantEmail");
                
                logger.info("Processing application delete event - ID: {}, Role: {}, Email: {}", id, initiatorRole, initiatorEmail);
                
                if ("Administrator".equals(initiatorRole)) {
                    // Administrators can delete any application
                    logger.info("Admin delete - removing application {}", id);
                    applicationRepository.deleteById(id);
                    logger.info("Successfully deleted application by admin - id: {}", id);
                    
                } else if (initiatorEmail != null) {
                    // Applicants can only delete their own applications
                    applicationRepository.findById(id).ifPresentOrElse(existing -> {
                        if (initiatorEmail.equals(existing.getApplicantEmail())) {
                            logger.info("Applicant delete - removing application {} for user {}", id, initiatorEmail);
                            applicationRepository.deleteById(id);
                            logger.info("Successfully deleted application by applicant - id: {}, user: {}", id, initiatorEmail);
                        } else {
                            logger.warn("Applicant {} tried to delete application {} owned by {}", 
                                      initiatorEmail, id, existing.getApplicantEmail());
                        }
                    }, () -> {
                        logger.warn("Application not found for deletion - id: {}, requestedBy: {}", id, initiatorEmail);
                    });
                    
                } else {
                    // If no initiator email provided, treat as applicant and try to delete
                    // This handles cases where the event might be missing the email field
                    logger.info("No initiator email provided - deleting application {}", id);
                    applicationRepository.deleteById(id);
                    logger.info("Successfully deleted application with no initiator email - id: {}", id);
                }
                
            } else {
                logger.warn("Unknown application event format - payload: {}", payload);
            }
            
        } catch (Exception e) {
            logger.error("Failed to process application event - payload: {}", payload, e);
        }
    }
}


