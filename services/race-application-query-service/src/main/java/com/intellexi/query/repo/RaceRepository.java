package com.intellexi.query.repo;

import com.intellexi.query.model.Race;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RaceRepository extends JpaRepository<Race, UUID> {}


