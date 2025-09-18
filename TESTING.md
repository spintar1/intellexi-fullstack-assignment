# Testing Guide
## Trail Race Registration System - CQRS Microservice Testing

This document provides comprehensive testing instructions for the Trail Race Registration System.

## 🏗️ Test Architecture

### Backend Testing (Spring Boot)
- **Unit Tests**: Individual component testing with mocks
- **Integration Tests**: Full Spring context with real database  
- **Mock Testing**: RabbitMQ, Security, Repositories
- **Test Profiles**: Separate configurations for testing

### Frontend Testing (React)
- **Component Tests**: React component behavior  
- **Integration Tests**: User interactions and API calls
- **Mock Testing**: API calls, localStorage, external dependencies
- **Coverage Reports**: Code coverage analysis

## 🚀 Quick Start

### Run All Tests
```bash
make test
```

### Run Specific Test Suites
```bash
# Backend only
make test-backend

# Frontend only  
make test-frontend

# Unit tests only
make test-unit

# Integration tests only
make test-integration
```

## 📋 Test Categories

### Backend Tests

#### Command Service Tests
- `RaceCommandControllerTest` - REST API endpoints
- `ApplicationCommandControllerTest` - Application commands
- `EventPublisherTest` - RabbitMQ event publishing
- `CommandServiceIntegrationTest` - Full integration tests

#### Query Service Tests  
- `RaceQueryControllerTest` - Race query endpoints
- `ApplicationQueryControllerTest` - Application queries
- `EventListenersTest` - RabbitMQ event consumption
- `QueryServiceIntegrationTest` - Database integration tests

### Frontend Tests
- `App.test.tsx` - Main application component
- `Races.test.tsx` - Race registration component

## 🔧 Test Configuration

### Backend Test Configuration
- **Database**: H2 in-memory database for Query Service
- **RabbitMQ**: Embedded test queues  
- **Security**: Mock JWT tokens for authentication
- **Profiles**: `application-test.yml` configurations

### Frontend Test Configuration
- **Environment**: jsdom for DOM testing
- **Mocks**: fetch API, localStorage, jwt-decode
- **Coverage**: Minimum 80% threshold

## 📊 Coverage Requirements

### Backend Coverage Goals
- **Lines**: 85%+
- **Branches**: 80%+
- **Functions**: 90%+

### Frontend Coverage Goals  
- **Lines**: 80%+
- **Branches**: 80%+
- **Functions**: 80%+

## 🔍 Test Examples

### Unit Test Example (Backend)
```java
@Test
void createRace_ValidRequest_ShouldCreateRace() throws Exception {
    var request = new CreateRaceRequest("Boston Marathon", "Marathon");
    
    mockMvc.perform(post("/api/v1/races")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists());
            
    verify(eventPublisher).publishRaceEvent(any(RaceCreated.class));
}
```

### Integration Test Example (Frontend)
```typescript
test('login form handles successful authentication', async () => {
  mockFetch.mockResolvedValueOnce({
    ok: true,
    json: () => Promise.resolve({ token: 'mock-jwt-token' }),
  });

  render(<App />);
  
  fireEvent.click(screen.getByText('Sign In'));
  
  await waitFor(() => {
    expect(mockFetch).toHaveBeenCalledWith(
      expect.stringContaining('/auth/token'),
      expect.objectContaining({ method: 'POST' })
    );
  });
});
```

## 🐛 Testing Best Practices

### Backend Testing
1. **Use `@MockBean` for Spring dependencies**
2. **Test both happy path and error cases**
3. **Verify event publishing in command tests**
4. **Test database constraints in integration tests**
5. **Use `@ActiveProfiles("test")` for test configurations**

### Frontend Testing
1. **Mock external dependencies (API, localStorage)**
2. **Test user interactions, not implementation details** 
3. **Use `waitFor` for async operations**
4. **Test accessibility features**
5. **Mock timers and network calls**

## 📈 Running Tests with Coverage

### Backend Coverage
```bash
cd services/race-application-command-service
mvn test jacoco:report

cd services/race-application-query-service  
mvn test jacoco:report
```

### Frontend Coverage
```bash
cd client
npm test -- --coverage
```

## 🔧 Debugging Tests

### Backend Test Debugging
```bash
# Run single test class
mvn test -Dtest=RaceCommandControllerTest

# Run with debug logging
mvn test -Dlogging.level.com.intellexi=DEBUG

# Run integration tests only
mvn test -Dtest="*IntegrationTest"
```

### Frontend Test Debugging  
```bash
# Watch mode
npm test -- --watch

# Run specific test file
npm test -- Races.test.tsx

# Verbose output
npm test -- --verbose
```

## 🚨 Test Failures & Troubleshooting

### Common Backend Issues
- **Database connection**: Check H2 test database configuration
- **RabbitMQ connection**: Verify test queue configuration
- **JWT tokens**: Ensure test secret key is configured
- **Spring context**: Check test profile activation

### Common Frontend Issues  
- **Mock setup**: Verify all external dependencies are mocked
- **Async operations**: Use `waitFor` for state changes
- **DOM cleanup**: Tests may interfere with each other
- **Import paths**: Check relative import paths

## 📝 Adding New Tests

### Backend Test Template
```java
@ExtendWith(MockitoExtension.class)
class NewServiceTest {
    
    @Mock
    private DependencyService dependencyService;
    
    private NewService newService;
    
    @BeforeEach
    void setUp() {
        newService = new NewService(dependencyService);
    }
    
    @Test
    void methodName_condition_expectedResult() {
        // Given
        // When  
        // Then
    }
}
```

### Frontend Test Template
```typescript  
import { render, screen, fireEvent } from '@testing-library/react';
import '@testing-library/jest-dom';
import NewComponent from '../NewComponent';

describe('NewComponent', () => {
  test('should render correctly', () => {
    render(<NewComponent />);
    
    expect(screen.getByText('Expected Text')).toBeInTheDocument();
  });
});
```

## 🎯 Test Strategy

### CQRS Testing Strategy
- **Command Side**: Focus on event publishing and business logic
- **Query Side**: Focus on data retrieval and event handling
- **Integration**: Test the complete event flow between services

### React Testing Strategy
- **Component Isolation**: Test components in isolation with mocks
- **User Interactions**: Test real user workflows
- **API Integration**: Mock API calls and test error handling

## 📦 Test Dependencies

### Backend Dependencies
- **JUnit 5**: Core testing framework
- **Mockito**: Mocking framework  
- **Spring Boot Test**: Spring testing utilities
- **TestContainers**: Optional for real database testing
- **H2 Database**: In-memory testing database

### Frontend Dependencies  
- **Jest**: Testing framework
- **React Testing Library**: React component testing
- **jsdom**: DOM simulation
- **jest-environment-jsdom**: Jest DOM environment

## ✅ Continuous Integration

For CI/CD pipelines, use:
```bash
# Full test suite with coverage
make test

# Production build with tests
make prod-build
```

The test suite is designed to run in Docker containers and CI/CD environments with proper exit codes and coverage reports.
