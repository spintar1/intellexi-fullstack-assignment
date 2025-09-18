package com.intellexi.command.api;

import com.intellexi.command.events.ApplicationEvents;
import com.intellexi.command.events.EventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@SpringJUnitConfig
class ApplicationCommandControllerTest {

    private MockMvc mockMvc;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private Authentication authentication;

    private ApplicationCommandController controller;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        controller = new ApplicationCommandController(eventPublisher);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void createApplication_ValidRequest_ShouldCreateApplication() throws Exception {
        // Given
        UUID raceId = UUID.randomUUID();
        var request = new ApplicationCommandController.ApplicationRequest(raceId);
        String requestJson = objectMapper.writeValueAsString(request);

        when(authentication.getPrincipal()).thenReturn("applicant@example.com");

        // When & Then
        mockMvc.perform(post("/api/v1/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .principal(authentication))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.message").value("Application submitted successfully. Note: you can only register once per race."));

        // Verify event was published
        ArgumentCaptor<ApplicationEvents.ApplicationCreated> eventCaptor = 
                ArgumentCaptor.forClass(ApplicationEvents.ApplicationCreated.class);
        verify(eventPublisher).publishApplicationEvent(eventCaptor.capture());

        ApplicationEvents.ApplicationCreated publishedEvent = eventCaptor.getValue();
        assertEquals(raceId, publishedEvent.getRaceId());
        assertEquals("applicant@example.com", publishedEvent.getApplicantEmail());
        assertNotNull(publishedEvent.getId());
    }

    @Test
    void createApplication_NullRaceId_ShouldReturnBadRequest() throws Exception {
        // Given
        var request = new ApplicationCommandController.ApplicationRequest(null);
        String requestJson = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(post("/api/v1/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .principal(authentication))
                .andExpect(status().isBadRequest());

        verify(eventPublisher, never()).publishApplicationEvent(any());
    }

    @Test
    void createApplication_EventPublishingFails_ShouldReturnInternalServerError() throws Exception {
        // Given
        UUID raceId = UUID.randomUUID();
        var request = new ApplicationCommandController.ApplicationRequest(raceId);
        String requestJson = objectMapper.writeValueAsString(request);
        
        when(authentication.getPrincipal()).thenReturn("applicant@example.com");
        doThrow(new RuntimeException("Publishing failed")).when(eventPublisher).publishApplicationEvent(any());

        // When & Then
        mockMvc.perform(post("/api/v1/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .principal(authentication))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Failed to submit application. You might already be registered for this race."));
    }

    @Test
    void deleteApplication_ValidRequest_ShouldDeleteApplication() throws Exception {
        // Given
        when(authentication.getPrincipal()).thenReturn("applicant@example.com");
        when(authentication.getAuthorities()).thenReturn(java.util.Collections.emptyList());

        // When & Then
        mockMvc.perform(delete("/api/v1/applications/550e8400-e29b-41d4-a716-446655440000")
                        .principal(authentication))
                .andExpect(status().isAccepted());

        // Verify event was published
        ArgumentCaptor<ApplicationEvents.ApplicationDeleted> eventCaptor = 
                ArgumentCaptor.forClass(ApplicationEvents.ApplicationDeleted.class);
        verify(eventPublisher).publishApplicationEvent(eventCaptor.capture());

        ApplicationEvents.ApplicationDeleted publishedEvent = eventCaptor.getValue();
        assertEquals(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"), publishedEvent.getId());
        assertEquals("applicant@example.com", publishedEvent.getApplicantEmail());
    }

    @Test
    void deleteApplication_InvalidUuid_ShouldReturnBadRequest() throws Exception {

        // When & Then
        mockMvc.perform(delete("/api/v1/applications/invalid-uuid")
                        .principal(authentication))
                .andExpect(status().isBadRequest());

        verify(eventPublisher, never()).publishApplicationEvent(any());
    }

    @Test
    void deleteApplication_EventPublishingFails_ShouldReturnInternalServerError() throws Exception {
        // Given
        when(authentication.getPrincipal()).thenReturn("applicant@example.com");
        when(authentication.getAuthorities()).thenReturn(java.util.Collections.emptyList());
        doThrow(new RuntimeException("Publishing failed")).when(eventPublisher).publishApplicationEvent(any());

        // When & Then
        mockMvc.perform(delete("/api/v1/applications/550e8400-e29b-41d4-a716-446655440000")
                        .principal(authentication))
                .andExpect(status().isInternalServerError());
    }
}
