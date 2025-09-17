# Trail Race Application - Full Stack CQRS Microservice

A complete full-stack application for managing trail race applications using CQRS (Command Query Responsibility Segregation) architecture with event-driven microservices.

## 🏃‍♂️ Overview

This application allows:
- **Administrators** to create, update, and delete races
- **Applicants** to apply to races and manage their applications
- **Real-time data synchronization** between services via RabbitMQ
- **Role-based authentication** using JWT tokens

## 🏗️ Architecture

### Microservices
- **Command Service** (Port 8081): Handles CREATE/UPDATE/DELETE operations, publishes events
- **Query Service** (Port 8082): Consumes events, maintains read-optimized data, serves GET requests
- **Client Application** (Port 5173): React frontend with role-based UI

### Infrastructure
- **PostgreSQL**: Single query database (command service is stateless)
- **RabbitMQ**: Message broker for event-driven communication
- **Docker Compose**: Complete development environment

## 🚀 Quick Start

### Prerequisites
- Docker and Docker Compose
- Git

### 1. Clone and Start
```bash
git clone https://github.com/spintar1/intellexi-fullstack-assignment.git
cd intellexi-fullstack-assignment

# Stop any running containers
docker compose down

# Build all services (recommended after first clone)
docker compose build --no-cache

# Start all services
docker compose up -d

# Check status
docker compose ps

# Open the application
start http://localhost:5173  # Windows
# or
open http://localhost:5173   # macOS/Linux
```

### 2. Access the Application
- **Web Application**: http://localhost:5173
- **Command API**: http://localhost:8081
- **Query API**: http://localhost:8082
- **RabbitMQ Management**: http://localhost:15672 (guest/guest)

### 3. Test the Application

#### Login as Administrator
- Email: `admin@example.com`
- Role: `Administrator`
- Create races, manage the system

#### Login as Applicant
- Email: `runner@example.com`
- Role: `Applicant`
- Apply to races, view your applications

## 🛠️ Development Commands

```bash
# Build all services
docker compose build

# Start all services
docker compose up -d

# View logs
docker compose logs -f

# Stop all services
docker compose down

# Rebuild and restart
docker compose down && docker compose build && docker compose up -d
```

## 🗄️ Database Access

### Connection Details

**Single PostgreSQL Database (Query Database):**
- **Host**: localhost
- **Port**: 5434
- **Database**: query_db
- **Username**: query
- **Password**: query

**Note**: The Command Service is stateless and doesn't use a database - it only publishes events to RabbitMQ.

### Using pgAdmin
1. Install pgAdmin
2. Register Server:
   - **Name**: Trail Race Query DB
   - **Host**: localhost
   - **Port**: 5434
   - **Database**: query_db
   - **Username**: query
   - **Password**: query
3. View tables: `races` and `applications`

## 🔧 API Endpoints

### Command Service (8081)
```
POST /auth/token                    # Get JWT token
POST /api/v1/races                  # Create race (Admin only)
PATCH /api/v1/races/{id}            # Update race (Admin only)
DELETE /api/v1/races/{id}           # Delete race (Admin only)
POST /api/v1/applications           # Create application (Admin/Applicant)
DELETE /api/v1/applications/{id}    # Delete application (Admin/Applicant)
```

### Query Service (8082)
```
GET /api/v1/races                   # List all races
GET /api/v1/races/{id}              # Get race by ID
GET /api/v1/applications            # List applications (filtered by role)
GET /api/v1/applications/{id}       # Get application by ID
```

## 🔐 Authentication

The application uses JWT tokens with role-based access:
- **Administrator**: Full access to all operations
- **Applicant**: Can create/delete their own applications, view races

### Getting a Token
```bash
curl -X POST http://localhost:8081/auth/token \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@example.com", "role": "Administrator"}'
```

## 📊 Data Flow

1. **Command Operations**: Client → Command Service → RabbitMQ Event
2. **Event Processing**: Query Service consumes events → Updates database
3. **Read Operations**: Client → Query Service → Database

## 🧪 Testing the Full Workflow

1. **Login as Administrator**
   - Create a race: "Spring Trail Run", Distance: "10k"
   - Try creating a duplicate race to test constraints

2. **Login as Applicant**
   - View available races
   - Apply to a race with your details
   - Try registering for the same race twice (see user-friendly error)
   - View your applications
   - Delete an application if needed

3. **Check Database**
   - Open pgAdmin
   - View the `races`, `applications`, and `users` tables
   - See data synchronized between services

## 🛡️ Data Integrity & Business Rules

The application enforces critical business rules at the database level:

### Database Constraints
- **Unique User Emails**: Prevents duplicate user accounts
- **Unique Race Names + Distance**: No duplicate races (e.g., two "Boston Marathon" 42.2km races)  
- **One Registration Per Race**: Users can only register once per race

### User-Friendly Error Handling
- **Smart Validation**: Frontend verifies registration success after submission
- **Clear Messages**: Instead of technical errors, users see helpful feedback
- **Maintains CQRS**: Command service stays stateless while providing great UX

### Example Error Messages
- ⚠️ "You are already registered for this race. Each participant can only register once per race."
- 🌐 "Network error. Please check your connection and try again."
- ✅ "Successfully registered for the race!"

## 🔍 Debugging & Development

### Enhanced Source Maps & Debug Configuration

The application is configured with comprehensive debugging support including source maps, debug logging, and IDE integration.

#### Quick Debug Setup

```bash
# Start only infrastructure for debugging
make debug-up

# In separate terminals or IDE:
# 1. Start Command Service with debugger
# 2. Start Query Service with debugger  
# 3. Start React app with source maps
make client-debug
```

#### VSCode Debugging

The project includes pre-configured VSCode launch configurations:

1. **Debug Command Service** - Launches with debug port 5005
2. **Debug Query Service** - Launches with debug port 5006  
3. **Debug React App** - Launches with source maps enabled
4. **Attach to Chrome** - For frontend debugging

**Usage:**
1. Open project in VSCode
2. Press `F5` or go to Run & Debug panel
3. Select desired configuration
4. Set breakpoints and debug

#### Source Maps Configuration

**Frontend:**
- **Development**: Inline source maps for immediate debugging
- **Production**: External source maps for security
- **CSS**: Source maps enabled for style debugging

**Backend:**
- **Debug information**: Lines, variables, and source included
- **Parameter names**: Preserved for reflection
- **Compiler warnings**: All enabled for better code quality

#### Enhanced Logging

Both services configured with debug-level logging:

```yaml
logging:
  level:
    com.intellexi: DEBUG
    org.springframework.security: DEBUG
    org.springframework.amqp: DEBUG
    org.hibernate.SQL: DEBUG
```

**Log Patterns:** Include timestamp, thread, level, class, line number, and message

#### Debug Commands

```bash
# Build with debug information
make build-debug

# Start React app with debugging
make client-debug

# View infrastructure logs only
make debug-logs

# Full debug environment
make debug-dev
```

#### IDE Integration

**IntelliJ IDEA:**
- Import Maven projects from `services/` directories
- Debug configurations auto-detected
- Source maps work out of the box

**VSCode:**
- Use provided `.vscode/launch.json`
- Java Extension Pack recommended
- ES6 modules and TypeScript supported

#### Browser Debugging

**Chrome DevTools:**
- Source maps automatically loaded
- Set breakpoints in original TypeScript
- Network tab shows API calls with full details
- React DevTools compatible

**Firefox:**
- Source maps supported
- Vue.js devtools compatible
- Network monitoring included

#### Remote Debugging

**Java Services:**
- Command Service: `localhost:5005`
- Query Service: `localhost:5006`
- Use IDE remote debug configuration

**Example IntelliJ Remote Debug:**
```
Host: localhost
Port: 5005 (or 5006)
Debugger mode: Attach to remote JVM
Transport: Socket
```

## 🐛 Troubleshooting

### Services Not Starting
```bash
# Check service status
docker compose ps

# View logs for specific service
docker compose logs race_application_query_service
```

### Database Connection Issues
- Ensure PostgreSQL container is running: `docker compose ps`
- Check port mapping: 5434 for query database
- Verify credentials: query/query for query_db
- Command service is stateless (no database connection)

### JWT Token Issues
- Tokens expire after 8 hours
- Get a new token from `/auth/token` endpoint
- Ensure JWT_SECRET is consistent across services

## 🏗️ Project Structure

```
├── client/                          # React frontend
│   ├── src/pages/                   # Application pages
│   └── Dockerfile
├── services/
│   ├── race-application-command-service/  # Command microservice
│   │   ├── src/main/java/com/intellexi/command/
│   │   └── Dockerfile
│   └── race-application-query-service/    # Query microservice
│       ├── src/main/java/com/intellexi/query/
│       └── Dockerfile
├── docker-compose.yml               # Complete environment
└── README.md
```

## 🚀 Production Considerations

- Replace default JWT secrets with secure random strings
- Use proper database credentials
- Enable HTTPS
- Add proper logging and monitoring
- Implement proper user authentication (not demo tokens)
- Add comprehensive tests
- Use environment-specific configurations

## 📝 Features Implemented

✅ **CQRS Architecture** with separate command/query services  
✅ **Event-Driven Communication** via RabbitMQ  
✅ **JWT Authentication** with role-based access  
✅ **React Frontend** with role-based UI  
✅ **PostgreSQL Database** with proper schema  
✅ **Database Constraints** preventing duplicates and enforcing business rules  
✅ **User-Friendly Error Messages** with smart frontend validation  
✅ **Docker Compose** development environment  
✅ **CORS Configuration** for cross-origin requests  
✅ **Database Migrations** (Flyway)  
✅ **RESTful APIs** following best practices  
✅ **Error Handling** and validation  

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

---

**Built with**: Spring Boot, React, PostgreSQL, RabbitMQ, Docker
