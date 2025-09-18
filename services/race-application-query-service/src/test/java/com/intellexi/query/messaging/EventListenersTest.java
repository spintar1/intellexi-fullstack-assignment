package com.intellexi.query.messaging;

import com.intellexi.query.model.Application;
import com.intellexi.query.model.Race;
import com.intellexi.query.model.User;
import com.intellexi.query.repo.ApplicationRepository;
import com.intellexi.query.repo.RaceRepository;
import com.intellexi.query.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventListenersTest {

    @Mock
    private RaceRepository raceRepository;
    
    @Mock
    private ApplicationRepository applicationRepository;
    
    @Mock
    private UserRepository userRepository;

    private EventListeners eventListeners;

    @BeforeEach
    void setUp() {
        eventListeners = new EventListeners(raceRepository, applicationRepository, userRepository);
    }

    @Test
    void handleRaceEvents_RaceCreated_ShouldCreateRace() {
        // Given
        UUID raceId = UUID.randomUUID();
        Map<String, Object> payload = Map.of(
            "id", raceId.toString(),
            "name", "Boston Marathon",
            "distance", "Marathon"
        );

        // When
        eventListeners.onRaceEvent(payload);

        // Then
        ArgumentCaptor<Race> raceCaptor = ArgumentCaptor.forClass(Race.class);
        verify(raceRepository).save(raceCaptor.capture());

        Race savedRace = raceCaptor.getValue();
        assertEquals(raceId, savedRace.getId());
        assertEquals("Boston Marathon", savedRace.getName());
        assertEquals("Marathon", savedRace.getDistance());
    }

    @Test
    void handleRaceEvents_RaceCreated_DuplicateRace_ShouldHandleConstraintViolation() {
        // Given
        UUID raceId = UUID.randomUUID();
        Map<String, Object> payload = Map.of(
            "id", raceId.toString(),
            "name", "Boston Marathon",
            "distance", "Marathon"
        );

        DataIntegrityViolationException exception = new DataIntegrityViolationException(
            "duplicate key value violates unique constraint \"uk_races_name_distance\"");
        when(raceRepository.save(any(Race.class))).thenThrow(exception);

        // When & Then - Should not throw exception but handle gracefully
        assertDoesNotThrow(() -> eventListeners.onRaceEvent(payload));
        verify(raceRepository).save(any(Race.class));
    }

    @Test
    void handleRaceEvents_RaceUpdated_ShouldUpdateRace() {
        // Given
        UUID raceId = UUID.randomUUID();
        Race existingRace = new Race(raceId, "Old Name", "5k");
        
        Map<String, Object> payload = Map.of(
            "id", raceId.toString(),
            "name", "Updated Name",
            "distance", "10k"
        );

        when(raceRepository.findById(raceId)).thenReturn(Optional.of(existingRace));

        // When
        eventListeners.onRaceEvent(payload);

        // Then
        ArgumentCaptor<Race> raceCaptor = ArgumentCaptor.forClass(Race.class);
        verify(raceRepository).save(raceCaptor.capture());

        Race updatedRace = raceCaptor.getValue();
        assertEquals("Updated Name", updatedRace.getName());
        assertEquals("10k", updatedRace.getDistance());
    }

    @Test
    void handleRaceEvents_RaceUpdated_RaceNotFound_ShouldLogWarning() {
        // Given
        UUID raceId = UUID.randomUUID();
        Map<String, Object> payload = Map.of(
            "id", raceId.toString(),
            "name", "Updated Name"
        );

        when(raceRepository.findById(raceId)).thenReturn(Optional.empty());

        // When
        eventListeners.onRaceEvent(payload);

        // Then
        verify(raceRepository, never()).save(any(Race.class));
    }

    @Test
    void handleRaceEvents_RaceDeleted_ShouldDeleteRace() {
        // Given
        UUID raceId = UUID.randomUUID();
        Map<String, Object> payload = Map.of("id", raceId.toString());

        // When
        eventListeners.onRaceEvent(payload);

        // Then
        verify(raceRepository).deleteById(raceId);
    }

    @Test
    void handleApplicationEvents_ApplicationCreated_ShouldCreateApplication() {
        // Given
        UUID appId = UUID.randomUUID();
        UUID raceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String email = "applicant@example.com";

        User user = new User(userId, "John", "Doe", email, 
                           LocalDate.of(1990, 1, 1), "Test Club", User.Role.Applicant);

        Map<String, Object> payload = Map.of(
            "id", appId.toString(),
            "raceId", raceId.toString(),
            "applicantEmail", email
        );

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // When
        eventListeners.onApplicationEvent(payload);

        // Then
        ArgumentCaptor<Application> appCaptor = ArgumentCaptor.forClass(Application.class);
        verify(applicationRepository).save(appCaptor.capture());

        Application savedApp = appCaptor.getValue();
        assertEquals(appId, savedApp.getId());
        assertEquals(raceId, savedApp.getRaceId());
        assertEquals(userId, savedApp.getUserId());
    }

    @Test
    void handleApplicationEvents_ApplicationCreated_DuplicateApplication_ShouldHandleConstraintViolation() {
        // Given
        UUID appId = UUID.randomUUID();
        UUID raceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String email = "applicant@example.com";

        User user = new User(userId, "John", "Doe", email, 
                           LocalDate.of(1990, 1, 1), "Test Club", User.Role.Applicant);

        Map<String, Object> payload = Map.of(
            "id", appId.toString(),
            "raceId", raceId.toString(),
            "applicantEmail", email
        );

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        DataIntegrityViolationException exception = new DataIntegrityViolationException(
            "duplicate key value violates unique constraint \"uk_applications_user_race\"");
        when(applicationRepository.save(any(Application.class))).thenThrow(exception);

        // When & Then - Should not throw exception but handle gracefully
        assertDoesNotThrow(() -> eventListeners.onApplicationEvent(payload));
        verify(applicationRepository).save(any(Application.class));
    }

    @Test
    void handleApplicationEvents_ApplicationCreated_UserNotFound_ShouldLogWarning() {
        // Given
        UUID appId = UUID.randomUUID();
        UUID raceId = UUID.randomUUID();
        String email = "nonexistent@example.com";

        Map<String, Object> payload = Map.of(
            "id", appId.toString(),
            "raceId", raceId.toString(),
            "applicantEmail", email
        );

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When
        eventListeners.onApplicationEvent(payload);

        // Then
        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    void handleApplicationEvents_ApplicationDeleted_ShouldDeleteApplication() {
        // Given
        UUID appId = UUID.randomUUID();
        Map<String, Object> payload = Map.of(
            "id", appId.toString(),
            "initiatorRole", "Administrator"
        );

        // When
        eventListeners.onApplicationEvent(payload);

        // Then
        verify(applicationRepository).deleteById(appId);
    }

    @Test
    void handleRaceEvents_InvalidPayload_ShouldHandleGracefully() {
        // Given
        Map<String, Object> invalidPayload = Map.of("invalid", "data");

        // When & Then
        assertDoesNotThrow(() -> eventListeners.onRaceEvent(invalidPayload));
        
        verify(raceRepository, never()).save(any());
        verify(raceRepository, never()).deleteById(any());
    }

    @Test
    void handleApplicationEvents_InvalidPayload_ShouldHandleGracefully() {
        // Given
        Map<String, Object> invalidPayload = Map.of("invalid", "data");

        // When & Then
        assertDoesNotThrow(() -> eventListeners.onApplicationEvent(invalidPayload));
        
        verify(applicationRepository, never()).save(any());
        verify(applicationRepository, never()).deleteById(any());
    }
}
