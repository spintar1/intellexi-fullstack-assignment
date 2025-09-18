package com.intellexi.query.api;

import com.intellexi.query.model.Race;
import com.intellexi.query.repo.RaceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@SpringJUnitConfig
class RaceQueryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RaceRepository raceRepository;

    private RaceQueryController controller;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        controller = new RaceQueryController(raceRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @WithMockUser(authorities = "ROLE_Applicant")
    void getAllRaces_ShouldReturnOrderedRaces() throws Exception {
        // Given
        UUID race1Id = UUID.randomUUID();
        UUID race2Id = UUID.randomUUID();
        Race race1 = new Race(race1Id, "Boston Marathon", "Marathon");
        Race race2 = new Race(race2Id, "Central Park 5K", "5k");
        
        when(raceRepository.findAllOrderedByName()).thenReturn(Arrays.asList(race1, race2));

        // When & Then
        mockMvc.perform(get("/api/v1/races"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(race1Id.toString()))
                .andExpect(jsonPath("$[0].name").value("Boston Marathon"))
                .andExpect(jsonPath("$[0].distance").value("Marathon"))
                .andExpect(jsonPath("$[1].id").value(race2Id.toString()))
                .andExpect(jsonPath("$[1].name").value("Central Park 5K"))
                .andExpect(jsonPath("$[1].distance").value("5k"));

        verify(raceRepository).findAllOrderedByName();
    }

    @Test
    @WithMockUser(authorities = "ROLE_Administrator")
    void getAllRaces_AsAdmin_ShouldReturnRaces() throws Exception {
        // Given
        when(raceRepository.findAllOrderedByName()).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/v1/races"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(authorities = "ROLE_Applicant")
    void getRaceById_ExistingRace_ShouldReturnRace() throws Exception {
        // Given
        UUID raceId = UUID.randomUUID();
        Race race = new Race(raceId, "Test Race", "10k");
        
        when(raceRepository.findById(raceId)).thenReturn(Optional.of(race));

        // When & Then
        mockMvc.perform(get("/api/v1/races/" + raceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(raceId.toString()))
                .andExpect(jsonPath("$.name").value("Test Race"))
                .andExpect(jsonPath("$.distance").value("10k"));

        verify(raceRepository).findById(raceId);
    }

    @Test
    @WithMockUser(authorities = "ROLE_Applicant")
    void getRaceById_NonExistentRace_ShouldReturnNotFound() throws Exception {
        // Given
        UUID raceId = UUID.randomUUID();
        when(raceRepository.findById(raceId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/v1/races/" + raceId))
                .andExpect(status().isNotFound());

        verify(raceRepository).findById(raceId);
    }

    @Test
    @WithMockUser(authorities = "ROLE_Applicant")
    void getRaceById_InvalidUuid_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/races/invalid-uuid"))
                .andExpect(status().isBadRequest());

        verify(raceRepository, never()).findById(any());
    }

    @Test
    void getAllRaces_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/races"))
                .andExpect(status().isUnauthorized());

        verify(raceRepository, never()).findAllOrderedByName();
    }

    @Test
    @WithMockUser(authorities = "ROLE_Applicant")
    void getAllRaces_RepositoryThrowsException_ShouldReturnInternalServerError() throws Exception {
        // Given
        when(raceRepository.findAllOrderedByName()).thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(get("/api/v1/races"))
                .andExpect(status().isInternalServerError());
    }
}
