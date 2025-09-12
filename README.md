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
- **PostgreSQL**: Two separate databases (command & query)
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
docker compose up -d
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

#### Command Database
- **Host**: localhost
- **Port**: 5433
- **Database**: command_db
- **Username**: command
- **Password**: command

#### Query Database
- **Host**: localhost
- **Port**: 5434
- **Database**: query_db
- **Username**: query
- **Password**: query

### Using pgAdmin
1. Install pgAdmin
2. Create server group: "Trail Race Databases"
3. Add both databases with the connection details above
4. View tables: `races` and `applications`

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

2. **Login as Applicant**
   - View available races
   - Apply to a race with your details
   - View your applications
   - Delete an application if needed

3. **Check Database**
   - Open pgAdmin
   - View the `races` and `applications` tables
   - See data synchronized between services

## 🐛 Troubleshooting

### Services Not Starting
```bash
# Check service status
docker compose ps

# View logs for specific service
docker compose logs race_application_query_service
```

### Database Connection Issues
- Ensure PostgreSQL containers are running
- Check port mappings (5433 for command, 5434 for query)
- Verify credentials match docker-compose.yml

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
✅ **PostgreSQL Databases** with proper schema  
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
