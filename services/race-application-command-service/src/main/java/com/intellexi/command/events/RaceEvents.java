package com.intellexi.command.events;

import java.util.UUID;

public class RaceEvents {
    public static class RaceCreated {
        private UUID id; private String name; private String distance;
        public RaceCreated() {}
        public RaceCreated(UUID id, String name, String distance) { this.id=id; this.name=name; this.distance=distance; }
        public UUID getId() { return id; }
        public String getName() { return name; }
        public String getDistance() { return distance; }
    }
    public static class RaceUpdated {
        private UUID id; private String name; private String distance;
        public RaceUpdated() {}
        public RaceUpdated(UUID id, String name, String distance) { this.id=id; this.name=name; this.distance=distance; }
        public UUID getId() { return id; }
        public String getName() { return name; }
        public String getDistance() { return distance; }
    }
    public static class RaceDeleted {
        private UUID id;
        public RaceDeleted() {}
        public RaceDeleted(UUID id) { this.id=id; }
        public UUID getId() { return id; }
    }
}


