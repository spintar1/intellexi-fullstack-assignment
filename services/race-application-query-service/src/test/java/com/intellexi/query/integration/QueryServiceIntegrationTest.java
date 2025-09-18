package com.intellexi.query.integration;

import com.intellexi.query.model.Application;
import com.intellexi.query.model.Race;
import com.intellexi.query.model.User;
import com.intellexi.query.repo.ApplicationRepository;
import com.intellexi.query.repo.RaceRepository;
import com.intellexi.query.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class QueryServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RaceRepository raceRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private UserRepository userRepository;

    private Race testRace;
    private User testUser;
    private Application testApplication;

    @BeforeEach
    void setUp() {
        // Clean up repositories
        applicationRepository.deleteAll();
        raceRepository.deleteAll();
        userRepository.deleteAll();

        // Create test data
        testRace = new Race(UUID.randomUUID(), "Integration Test Race", "10k");
        testRace = raceRepository.save(testRace);

        testUser = new User(UUID.randomUUID(), "Test", "User", "test@example.com",
                          LocalDate.of(1990, 1, 1), "Test Club", User.Role.Applicant);
        testUser = userRepository.save(testUser);

        testApplication = new Application(UUID.randomUUID(), testRace.getId(), testUser.getId());
        testApplication = applicationRepository.save(testApplication);
    }

    @Test
    @WithMockUser(authorities = "ROLE_Applicant")
    void getRaces_IntegrationTest_ShouldReturnRacesOrderedByName() throws Exception {
        // Given - additional race to test ordering
        Race anotherRace = new Race(UUID.randomUUID(), "Another Race", "5k");
        raceRepository.save(anotherRace);

        // When & Then
        mockMvc.perform(get("/api/v1/races"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Another Race"))
                .andExpect(jsonPath("$[1].name").value("Integration Test Race"));
    }

    @Test
    @WithMockUser(authorities = "ROLE_Applicant")
    void getRaceById_IntegrationTest_ShouldReturnSpecificRace() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/races/" + testRace.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testRace.getId().toString()))
                .andExpect(jsonPath("$.name").value("Integration Test Race"))
                .andExpect(jsonPath("$.distance").value("10k"));
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = "ROLE_Applicant")
    void getApplications_AsApplicant_IntegrationTest_ShouldReturnOwnApplications() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/applications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(testApplication.getId().toString()))
                .andExpect(jsonPath("$[0].raceId").value(testRace.getId().toString()));
    }

    @Test
    @WithMockUser(username = "admin@example.com", authorities = "ROLE_Administrator")
    void getApplications_AsAdmin_IntegrationTest_ShouldReturnAllApplications() throws Exception {
        // Given - create admin user
        User adminUser = new User(UUID.randomUUID(), "Admin", "User", "admin@example.com",
                                LocalDate.of(1985, 1, 1), null, User.Role.Administrator);
        userRepository.save(adminUser);

        // When & Then
        mockMvc.perform(get("/api/v1/applications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser(authorities = "ROLE_Applicant")
    void getApplicationById_IntegrationTest_ShouldReturnSpecificApplication() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/applications/" + testApplication.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testApplication.getId().toString()))
                .andExpect(jsonPath("$.raceId").value(testRace.getId().toString()))
                .andExpect(jsonPath("$.userId").value(testUser.getId().toString()));
    }

    @Test
    void getRaces_WithoutAuthentication_IntegrationTest_ShouldReturnUnauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/races"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "ROLE_Applicant")
    void getRace_NonExistentId_IntegrationTest_ShouldReturnNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/races/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "ROLE_Applicant")
    void getApplication_NonExistentId_IntegrationTest_ShouldReturnNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/applications/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "nonexistent@example.com", authorities = "ROLE_Applicant")
    void getApplications_NonExistentUser_IntegrationTest_ShouldReturnNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/applications"))
                .andExpect(status().isNotFound());
    }
}
