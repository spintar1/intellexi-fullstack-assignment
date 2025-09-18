package com.intellexi.command.api;

import com.intellexi.command.events.EventPublisher;
import com.intellexi.command.events.RaceEvents;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@SpringJUnitConfig
class RaceCommandControllerTest {

    private MockMvc mockMvc;

    @Mock
    private EventPublisher eventPublisher;

    private RaceCommandController controller;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        controller = new RaceCommandController(eventPublisher);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void createRace_ValidRequest_ShouldCreateRace() throws Exception {
        // Given
        var request = new RaceCommandController.CreateRaceRequest("Boston Marathon", "Marathon");
        String requestJson = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(post("/api/v1/races")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.message").value("Race created successfully. Note: duplicate races with the same name and distance are not allowed."));

        // Verify event was published
        ArgumentCaptor<RaceEvents.RaceCreated> eventCaptor = ArgumentCaptor.forClass(RaceEvents.RaceCreated.class);
        verify(eventPublisher).publishRaceEvent(eventCaptor.capture());

        RaceEvents.RaceCreated publishedEvent = eventCaptor.getValue();
        assertEquals("Boston Marathon", publishedEvent.getName());
        assertEquals("Marathon", publishedEvent.getDistance());
        assertNotNull(publishedEvent.getId());
    }

    @Test
    void createRace_InvalidDistance_ShouldReturnBadRequest() throws Exception {
        // Given
        var request = new RaceCommandController.CreateRaceRequest("Test Race", "InvalidDistance");
        String requestJson = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(post("/api/v1/races")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid distance"));

        // Verify no event was published
        verify(eventPublisher, never()).publishRaceEvent(any());
    }

    @Test
    void createRace_EmptyName_ShouldReturnBadRequest() throws Exception {
        // Given
        var request = new RaceCommandController.CreateRaceRequest("", "Marathon");
        String requestJson = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(post("/api/v1/races")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());

        verify(eventPublisher, never()).publishRaceEvent(any());
    }

    @Test
    void updateRace_ValidRequest_ShouldUpdateRace() throws Exception {
        // Given
        var request = new RaceCommandController.UpdateRaceRequest("Updated Race", "10k");
        String requestJson = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(patch("/api/v1/races/550e8400-e29b-41d4-a716-446655440000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk());

        // Verify event was published
        ArgumentCaptor<RaceEvents.RaceUpdated> eventCaptor = ArgumentCaptor.forClass(RaceEvents.RaceUpdated.class);
        verify(eventPublisher).publishRaceEvent(eventCaptor.capture());

        RaceEvents.RaceUpdated publishedEvent = eventCaptor.getValue();
        assertEquals("Updated Race", publishedEvent.getName());
        assertEquals("10k", publishedEvent.getDistance());
    }

    @Test
    void deleteRace_ValidRequest_ShouldDeleteRace() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/v1/races/550e8400-e29b-41d4-a716-446655440000"))
                .andExpect(status().isNoContent());

        // Verify event was published
        ArgumentCaptor<RaceEvents.RaceDeleted> eventCaptor = ArgumentCaptor.forClass(RaceEvents.RaceDeleted.class);
        verify(eventPublisher).publishRaceEvent(eventCaptor.capture());

        RaceEvents.RaceDeleted publishedEvent = eventCaptor.getValue();
        assertEquals("550e8400-e29b-41d4-a716-446655440000", publishedEvent.getId().toString());
    }

    @Test
    void deleteRace_InvalidUuid_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/v1/races/invalid-uuid"))
                .andExpect(status().isBadRequest());

        verify(eventPublisher, never()).publishRaceEvent(any());
    }

    @Test
    void createRace_EventPublishingFails_ShouldReturnInternalServerError() throws Exception {
        // Given
        var request = new RaceCommandController.CreateRaceRequest("Test Race", "Marathon");
        String requestJson = objectMapper.writeValueAsString(request);
        
        doThrow(new RuntimeException("Publishing failed")).when(eventPublisher).publishRaceEvent(any());

        // When & Then
        mockMvc.perform(post("/api/v1/races")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Failed to create race. This might be due to a duplicate race with the same name and distance."));
    }
}
