# Event RSVP and Attendance Manager - Backend

A Spring Boot 3 REST API for managing events, RSVPs, attendees, categories, and check-ins with JWT authentication and attendance forecasting using linear regression.

## Student Information

- Student: Mahek Naaz
- Student ID: 24217808
- Module: Cloud DevOpsSec (H9CDOS)

## Tech Stack

- Java 17
- Spring Boot 3.2.5
- Spring Security 6 with JWT Authentication
- Spring Data JPA with MySQL
- Apache Commons Math 3 (SimpleRegression for attendance forecasting)
- Springdoc OpenAPI (Swagger UI)
- Maven for build and dependency management

## Project Structure

```
src/main/java/com/eventmanager/eventrsvp/
    config/         Security, CORS, JWT, and OpenAPI configuration
    controller/     REST API controllers for all endpoints
    dto/            Data Transfer Objects with Jakarta validation
    model/          JPA entities and enums
    repository/     Spring Data JPA repositories
    service/        Business logic layer
    exception/      Global exception handler and custom exceptions
```

## Entities

- User: Application users with JWT authentication
- Event: Events with title, date, time, location, capacity, status
- Attendee: People who can RSVP to events
- Rsvp: RSVP responses linking attendees to events
- Category: Event categories for organisation
- CheckIn: Attendance records for event check-ins

## API Endpoints

### Authentication
- POST /api/auth/register - Register a new user
- POST /api/auth/login - Login and receive JWT token

### Events (Full CRUD)
- GET /api/events - List all events
- GET /api/events/{id} - Get event by ID
- GET /api/events/status/{status} - Filter by status
- GET /api/events/category/{categoryId} - Filter by category
- GET /api/events/search?title= - Search by title
- POST /api/events - Create event (authenticated)
- PUT /api/events/{id} - Update event (authenticated)
- DELETE /api/events/{id} - Delete event (authenticated)

### Attendees (Full CRUD)
- GET /api/attendees - List all attendees
- GET /api/attendees/{id} - Get attendee by ID
- GET /api/attendees/search?query= - Search by name
- POST /api/attendees - Create attendee (authenticated)
- PUT /api/attendees/{id} - Update attendee (authenticated)
- DELETE /api/attendees/{id} - Delete attendee (authenticated)

### Categories (Full CRUD)
- GET /api/categories - List all categories
- GET /api/categories/{id} - Get category by ID
- POST /api/categories - Create category (authenticated)
- PUT /api/categories/{id} - Update category (authenticated)
- DELETE /api/categories/{id} - Delete category (authenticated)

### RSVPs (Full CRUD)
- GET /api/rsvps - List all RSVPs
- GET /api/rsvps/{id} - Get RSVP by ID
- GET /api/rsvps/event/{eventId} - RSVPs by event
- GET /api/rsvps/attendee/{attendeeId} - RSVPs by attendee
- POST /api/rsvps - Create RSVP (authenticated)
- PUT /api/rsvps/{id} - Update RSVP (authenticated)
- DELETE /api/rsvps/{id} - Delete RSVP (authenticated)

### Check-ins
- GET /api/checkins - List all check-ins
- GET /api/checkins/{id} - Get check-in by ID
- GET /api/checkins/event/{eventId} - Check-ins by event
- POST /api/checkins - Create check-in (authenticated)
- DELETE /api/checkins/{id} - Delete check-in (authenticated)

### Dashboard and Analytics
- GET /api/dashboard - Dashboard statistics and charts data
- GET /api/forecast - Attendance forecasting predictions
- GET /api/health - Health check endpoint

## Running Locally

### Prerequisites
- Java 17 or higher
- Maven 3.8+
- MySQL 8.0+

### Setup
1. Create a MySQL database: `CREATE DATABASE event_rsvp_db;`
2. Update database credentials in `src/main/resources/application.properties`
3. Run the application:
```bash
mvn spring-boot:run
```
4. Access Swagger UI: http://localhost:8080/swagger-ui.html

## Static Analysis Tools

- SpotBugs: Bug detection in Java code
- PMD: Code style and complexity analysis
- JaCoCo: Code coverage measurement (minimum 60%)

Run all analysis tools:
```bash
mvn verify
```

## Testing

Run tests with H2 in-memory database:
```bash
mvn test
```

## CI/CD Pipeline

The CI/CD pipeline is configured in `.github/workflows/ci-cd.yml` and runs:
- Build and test
- Static analysis (SpotBugs, PMD, JaCoCo)
- Security vulnerability scanning (Trivy)
- Deployment to AWS EC2 (on push to main)

## Cloud Deployment

- Compute: AWS EC2 (t2.micro)
- Database: AWS RDS MySQL (db.t3.micro)
- Infrastructure as Code: Terraform (see terraform/ directory)
