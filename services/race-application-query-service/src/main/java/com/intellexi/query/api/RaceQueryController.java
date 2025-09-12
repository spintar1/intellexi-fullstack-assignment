package com.intellexi.query.api;

import com.intellexi.query.model.Race;
import com.intellexi.query.repo.RaceRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/races")
public class RaceQueryController {
    private final RaceRepository raceRepository;

    public RaceQueryController(RaceRepository raceRepository) {
        this.raceRepository = raceRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('Applicant','Administrator')")
    public List<Race> all() { return raceRepository.findAll(); }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('Applicant','Administrator')")
    public ResponseEntity<Race> one(@PathVariable UUID id) {
        return raceRepository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}


