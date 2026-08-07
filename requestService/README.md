# Request Service

A Spring Boot microservice for managing **cross-library book borrowing requests**, backed by a standalone **Redis** key-value database.

---

## Requirements

- Java 21
- Maven 3.8+
- Redis running on `localhost:6379` (or via Docker)

---

## Run Redis (Docker)

```bash
docker run -d --name redis -p 6379:6379 redis
```

---

## Run the Application

```bash
./mvnw spring-boot:run
```

Server starts on `http://localhost:8080`

---

## Project Structure

```
src/main/java/com/library/requestservice/
├── config/
│   └── RedisConfig.java              # Redis + Jackson serialization setup
├── controller/
│   └── BorrowRequestController.java  # REST endpoints
├── dto/
│   ├── CreateBorrowRequestDto.java   # Input DTO for creating requests
│   └── UpdateBorrowRequestDto.java   # Input DTO for updating requests
├── exception/
│   ├── GlobalExceptionHandler.java   # Centralized error responses
│   ├── InvalidStatusTransitionException.java
│   └── RequestNotFoundException.java
├── model/
│   ├── BorrowRequest.java            # Main domain model
│   └── RequestStatus.java            # PENDING, ACCEPTED, DENIED, CANCELLED
├── repository/
│   └── BorrowRequestRepository.java  # Redis CRUD operations
└── service/
    └── BorrowRequestService.java     # Business logic
```

---

## Data Model

| Field                   | Type          | Notes                                  |
|------------------------|---------------|----------------------------------------|
| id                     | String (UUID) | Auto-generated                         |
| senderId               | String        | ID of the librarian sending request    |
| senderLibraryId        | String        | Library the sender works at            |
| receiverLibraryId      | String        | Library receiving the request          |
| respondedByLibrarianId | String        | Null until request is responded to     |
| description            | String        | 5–300 characters                       |
| status                 | RequestStatus | PENDING → ACCEPTED / DENIED / CANCELLED|
| createdAt              | Instant       | Set on creation                        |
| updatedAt              | Instant       | Updated on every change                |

---

## API Endpoints

### Create a request
```
POST /api/requests
Content-Type: application/json

{
  "senderId": "librarian-001",
  "senderLibraryId": "lib-novi-sad",
  "receiverLibraryId": "lib-beograd",
  "description": "Requesting 'The Name of the Wind' for 14 days."
}
```

### Get a request by ID
```
GET /api/requests/{id}
```

### Get all requests
```
GET /api/requests
```

### Update a request (accept / deny / cancel)
```
PUT /api/requests/{id}
Content-Type: application/json

{
  "status": "ACCEPTED",
  "respondedByLibrarianId": "librarian-042"
}
```
> `respondedByLibrarianId` is **required** when status is `ACCEPTED` or `DENIED`.  
> For `CANCELLED`, it is optional.

### Delete a request
```
DELETE /api/requests/{id}
```

---

## Status Transition Rules

```
PENDING ──► ACCEPTED
        ──► DENIED
        ──► CANCELLED
```
- Only `PENDING` requests can be updated.
- Once a request is `ACCEPTED`, `DENIED`, or `CANCELLED`, it is **terminal** and cannot change.

---

## Redis Key Format

Requests are stored in Redis under the key pattern:

```
library:request:{uuid}
```
