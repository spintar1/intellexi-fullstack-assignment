package com.intellexi.query.dto;

import java.util.UUID;

public class ApplicationWithUserDto {
    private UUID id;
    private UUID raceId;
    private UUID userId;
    private String firstName;
    private String lastName;
    private String email;
    private String club;

    public ApplicationWithUserDto() {}

    public ApplicationWithUserDto(UUID id, UUID raceId, UUID userId, String firstName, String lastName, String email, String club) {
        this.id = id;
        this.raceId = raceId;
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.club = club;
    }

    // Getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getRaceId() { return raceId; }
    public void setRaceId(UUID raceId) { this.raceId = raceId; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getClub() { return club; }
    public void setClub(String club) { this.club = club; }
}
