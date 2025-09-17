package com.intellexi.command.events;

import java.util.UUID;

public class ApplicationEvents {
    public static class ApplicationCreated {
        private UUID id; private UUID raceId; private String applicantEmail;
        public ApplicationCreated() {}
        public ApplicationCreated(UUID id, UUID raceId, String applicantEmail) {
            this.id=id; this.raceId=raceId; this.applicantEmail=applicantEmail;
        }
        public UUID getId() { return id; }
        public UUID getRaceId() { return raceId; }
        public String getApplicantEmail() { return applicantEmail; }
    }
    public static class ApplicationDeleted {
        private UUID id; private String applicantEmail; private String initiatorRole;
        public ApplicationDeleted() {}
        public ApplicationDeleted(UUID id, String applicantEmail, String initiatorRole) { this.id=id; this.applicantEmail=applicantEmail; this.initiatorRole=initiatorRole; }
        public UUID getId() { return id; }
        public String getApplicantEmail() { return applicantEmail; }
        public String getInitiatorRole() { return initiatorRole; }
    }
}


