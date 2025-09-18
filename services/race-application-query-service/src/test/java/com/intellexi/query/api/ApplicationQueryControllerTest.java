package com.intellexi.query.api;

import com.intellexi.query.model.Application;
import com.intellexi.query.model.User;
import com.intellexi.query.repo.ApplicationRepository;
import com.intellexi.query.repo.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@SpringJUnitConfig
class ApplicationQueryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ApplicationRepository applicationRepository;
    
    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    private ApplicationQueryController controller;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        controller = new ApplicationQueryController(applicationRepository, userRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @WithMockUser(username = "applicant@example.com", authorities = "ROLE_Applicant")
    void getAllApplications_AsApplicant_ShouldReturnOwnApplications() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        UUID appId = UUID.randomUUID();
        UUID raceId = UUID.randomUUID();
        
        User user = new User(userId, "John", "Doe", "applicant@example.com", 
                           LocalDate.of(1990, 1, 1), "Test Club", User.Role.Applicant);
        Application app = new Application(appId, raceId, userId);
        
        when(authentication.getPrincipal()).thenReturn("applicant@example.com");
        when(userRepository.findByEmail("applicant@example.com")).thenReturn(Optional.of(user));
        when(applicationRepository.findByUserId(userId)).thenReturn(Arrays.asList(app));

        // When & Then
        mockMvc.perform(get("/api/v1/applications")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(appId.toString()))
                .andExpect(jsonPath("$[0].raceId").value(raceId.toString()))
                .andExpect(jsonPath("$[0].userId").value(userId.toString()));

        verify(userRepository).findByEmail("applicant@example.com");
        verify(applicationRepository).findByUserId(userId);
    }

    @Test
    @WithMockUser(username = "admin@example.com", authorities = "ROLE_Administrator")  
    void getAllApplications_AsAdmin_ShouldReturnAllApplications() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "Admin", "User", "admin@example.com", 
                           LocalDate.of(1985, 1, 1), null, User.Role.Administrator);
        
        when(authentication.getPrincipal()).thenReturn("admin@example.com");
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(user));
        when(applicationRepository.findAll()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/v1/applications")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(userRepository).findByEmail("admin@example.com");
        verify(applicationRepository).findAll();
    }

    @Test
    @WithMockUser(authorities = "ROLE_Applicant")
    void getApplicationById_ExistingApplication_ShouldReturnApplication() throws Exception {
        // Given
        UUID appId = UUID.randomUUID();
        UUID raceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Application app = new Application(appId, raceId, userId);
        
        when(applicationRepository.findById(appId)).thenReturn(Optional.of(app));

        // When & Then
        mockMvc.perform(get("/api/v1/applications/" + appId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(appId.toString()))
                .andExpect(jsonPath("$.raceId").value(raceId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()));

        verify(applicationRepository).findById(appId);
    }

    @Test
    @WithMockUser(authorities = "ROLE_Applicant")
    void getApplicationById_NonExistentApplication_ShouldReturnNotFound() throws Exception {
        // Given
        UUID appId = UUID.randomUUID();
        when(applicationRepository.findById(appId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/v1/applications/" + appId))
                .andExpect(status().isNotFound());

        verify(applicationRepository).findById(appId);
    }

    @Test
    void getAllApplications_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/applications"))
                .andExpect(status().isUnauthorized());

        verify(applicationRepository, never()).findAll();
        verify(applicationRepository, never()).findByUserId(any());
    }

    @Test
    @WithMockUser(username = "applicant@example.com", authorities = "ROLE_Applicant")
    void getAllApplications_UserNotFound_ShouldReturnNotFound() throws Exception {
        // Given
        when(authentication.getPrincipal()).thenReturn("applicant@example.com");
        when(userRepository.findByEmail("applicant@example.com")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/v1/applications")
                        .principal(authentication))
                .andExpect(status().isNotFound());

        verify(userRepository).findByEmail("applicant@example.com");
        verify(applicationRepository, never()).findByUserId(any());
    }
}
