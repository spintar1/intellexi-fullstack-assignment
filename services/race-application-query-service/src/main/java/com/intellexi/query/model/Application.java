package com.intellexi.query.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "applications")
public class Application {
    @Id
    private UUID id;
    private String firstName;
    private String lastName;
    private String club;
    private UUID raceId;
    private String applicantEmail;

    public Application() {}
    public Application(UUID id, String firstName, String lastName, String club, UUID raceId, String applicantEmail) {
        this.id = id; this.firstName = firstName; this.lastName = lastName; this.club = club; this.raceId = raceId; this.applicantEmail = applicantEmail;
    }
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getClub() { return club; }
    public void setClub(String club) { this.club = club; }
    public UUID getRaceId() { return raceId; }
    public void setRaceId(UUID raceId) { this.raceId = raceId; }
    public String getApplicantEmail() { return applicantEmail; }
    public void setApplicantEmail(String applicantEmail) { this.applicantEmail = applicantEmail; }
}


