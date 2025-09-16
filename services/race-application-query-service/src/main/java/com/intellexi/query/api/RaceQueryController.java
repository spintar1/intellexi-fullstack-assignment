package com.intellexi.query.api;

import com.intellexi.query.model.Race;
import com.intellexi.query.repo.RaceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/races")
public class RaceQueryController {
    private static final Logger logger = LoggerFactory.getLogger(RaceQueryController.class);
    private final RaceRepository raceRepository;

    public RaceQueryController(RaceRepository raceRepository) {
        this.raceRepository = raceRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('Applicant','Administrator')")
    public List<Race> all() {
        logger.info("Received races query request - fetching all races");
        
        try {
            List<Race> races = raceRepository.findAllOrderedByName();
            logger.info("Successfully retrieved {} races ordered by name", races.size());
            return races;
        } catch (Exception e) {
            logger.error("Failed to retrieve all races", e);
            throw e;
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('Applicant','Administrator')")
    public ResponseEntity<Race> one(@PathVariable UUID id) {
        logger.info("Received single race query request - id: {}", id);
        
        try {
            return raceRepository.findById(id)
                    .map(race -> {
                        logger.info("Successfully retrieved race - id: {}, name: '{}'", id, race.getName());
                        return ResponseEntity.ok(race);
                    })
                    .orElseGet(() -> {
                        logger.warn("Race not found - id: {}", id);
                        return ResponseEntity.notFound().build();
                    });
        } catch (Exception e) {
            logger.error("Failed to retrieve race - id: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}


