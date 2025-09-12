package com.intellexi.query.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "races")
public class Race {
    @Id
    private UUID id;
    private String name;
    private String distance;

    public Race() {}
    public Race(UUID id, String name, String distance) { this.id = id; this.name = name; this.distance = distance; }
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDistance() { return distance; }
    public void setDistance(String distance) { this.distance = distance; }
}


