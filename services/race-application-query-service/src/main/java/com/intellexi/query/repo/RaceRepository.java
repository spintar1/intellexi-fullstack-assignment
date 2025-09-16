package com.intellexi.query.repo;

import com.intellexi.query.model.Race;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface RaceRepository extends JpaRepository<Race, UUID> {
    @Query("SELECT r FROM Race r ORDER BY r.name ASC")
    List<Race> findAllOrderedByName();
}


