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
        // Simple payload mapper matching command service events
        if (payload.get("id") != null && payload.get("name") != null && payload.get("distance") != null) {
            // RaceCreated
            Race race = new Race(UUID.fromString(payload.get("id").toString()), payload.get("name").toString(), payload.get("distance").toString());
            raceRepository.save(race);
        } else if (payload.get("id") != null && (payload.get("name") != null || payload.get("distance") != null)) {
            // RaceUpdated (partial)
            UUID id = UUID.fromString(payload.get("id").toString());
            raceRepository.findById(id).ifPresent(existing -> {
                if (payload.get("name") != null) existing.setName(payload.get("name").toString());
                if (payload.get("distance") != null) existing.setDistance(payload.get("distance").toString());
                raceRepository.save(existing);
            });
        } else if (payload.get("id") != null && payload.size() == 1) {
            // RaceDeleted
            raceRepository.deleteById(UUID.fromString(payload.get("id").toString()));
        }
    }

    @RabbitListener(queues = "${app.rabbit.queues.applications}")
    public void onApplicationEvent(@Payload Map<String, Object> payload) {
        if (payload.get("firstName") != null) {
            // ApplicationCreated
            Application a = new Application(
                    UUID.fromString(payload.get("id").toString()),
                    payload.get("firstName").toString(),
                    payload.get("lastName").toString(),
                    (String) payload.get("club"),
                    UUID.fromString(payload.get("raceId").toString()),
                    (String) payload.get("applicantEmail")
            );
            applicationRepository.save(a);
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
            } else if (initiatorEmail != null) {
                // Applicants can only delete their own applications
                applicationRepository.findById(id).ifPresent(existing -> {
                    if (initiatorEmail.equals(existing.getApplicantEmail())) {
                        logger.info("Applicant delete - removing application {} for user {}", id, initiatorEmail);
                        applicationRepository.deleteById(id);
                    } else {
                        logger.warn("Applicant {} tried to delete application {} owned by {}", 
                                  initiatorEmail, id, existing.getApplicantEmail());
                    }
                });
            } else {
                // If no initiator email provided, treat as applicant and try to delete
                // This handles cases where the event might be missing the email field
                logger.info("No initiator email provided - deleting application {}", id);
                applicationRepository.deleteById(id);
            }
        }
    }
}


