# Swift Codes API

---

### Author: Szymon Biel

## Remitly 2025 Internship Recruitment Task


### This is a Spring Boot application designed for parsing, storing, and exposing SWIFT code data via REST API. It is built with Java 21, uses PostgreSQL as its database, and supports Docker for containerized deployment. The application automatically loads data from an Excel file at startup and provides endpoints for data access and manipulation.

---

## 1. Used Technologies

- **Java 21 + Spring Boot 3**

- **PostgreSQ** as database

- **Apache POI** for Excel parsing

- **Spring Data JPA** for persistence

- **Docker + Docker Compose** for containerization

- **JUnit 5 + Mockito + MockMvc** for unit and integration testing

## 2. Application Setup and Running

1. **Clone the Repository:**

```
git clone https://github.com/your-username/swift-codes-api.git
cd swift-codes-api
```

2. **Build and Run using Docker:**
```
docker-compose up --build
```
3. **After building, the application should be available at:**
```
http://localhost:8080
```

**File Locations:**

Excel file (swiftCodes.xlsx) should be in: src/main/resources/
It will be automatically imported at startup if the database is empty.

---

## 3. API Endpoints

### 3.1 `GET /v1/swift-codes/{swiftCode}`

Retrieve details of a SWIFT code. If it ends with `XXX`, the response will include the branches for that headquarters.

**Headquarter Response Example:**
```json
{
  "address": "string",
  "bankName": "string",
  "countryISO2": "string",
  "countryName": "string",
  "isHeadquarter": true,
  "swiftCode": "ABCDEF12XXX",
  "branches": [
    {
      "address": "string",
      "bankName": "string",
      "countryISO2": "string",
      "isHeadquarter": false,
      "swiftCode": "ABCDEF12XYZ"
    },
    {
      "address": "string",
      "bankName": "string",
      "countryISO2": "string",
      "isHeadquarter": false,
      "swiftCode": "ABCDEF12XY2"
    }
  ]
}
```

**Branch Response Example:**
```json
{
  "address": "string",
  "bankName": "string",
  "countryISO2": "string",
  "countryName": "string",
  "isHeadquarter": false,
  "swiftCode": "ABCDEF12XYZ"
}
```

---

### 3.2 `GET /v1/swift-codes/country/{countryISO2}`

Returns all SWIFT codes for a specific country (headquarters and branches).

**Example Response:**
```json
{
  "countryISO2": "PL",
  "countryName": "POLAND",
  "swiftCodes": [
    {
      "address": "string",
      "bankName": "string",
      "countryISO2": "PL",
      "isHeadquarter": true,
      "swiftCode": "ABCDEFPLXXX"
    },
    {
      "address": "string",
      "bankName": "string",
      "countryISO2": "PL",
      "isHeadquarter": false,
      "swiftCode": "ABCDEFPL002"
    }
  ]
}
```

---

### 3.3 `POST /v1/swift-codes`

Adds a new SWIFT code entry to the database.

**Request Body Example:**
```json
{
  "address": "123 Sample Street",
  "bankName": "Example Bank",
  "countryISO2": "US",
  "countryName": "UNITED STATES",
  "isHeadquarter": true,
  "swiftCode": "EXAMUSNYXXX"
}
```

**Success Response:**
```json
{
  "message": "Swift code created"
}
```

---

### 3.4 `DELETE /v1/swift-codes/{swiftCode}`

Deletes the SWIFT code entry that matches the given `swiftCode`.

**Success Response:**
```json
{
  "message": "Swift code deleted"
}
```

---

## 4. Error Handling

The API provides meaningful error messages and HTTP status codes for the following scenarios:

### 4.1 SWIFT code not found
**Request:**
```http
GET /v1/swift-codes/NONEXISTENT
```
**Response:**
```json
{
  "message": "SwiftCode not found with swiftCode: 'NONEXISTENT'"
}
```
**Status:** `404 Not Found`

### 4.2 Duplicate SWIFT code on creation
**Request:** (POSTing an already existing swiftCode)
```json
{
  "swiftCode": "EXAMUSNYXXX",
  ...
}
```
**Response:**
```json
{
  "message": "SwiftCode already exists with swiftCode: 'EXAMUSNYXXX'"
}
```
**Status:** `409 Conflict`

### 4.3 Missing or invalid fields in request body
**Request:**
```json
{
  "swiftCode": "",
  "bankName": "",
  "countryISO2": "US"
}
```
**Response:**
```json
{
  "errors": {
    "address": "Address cannot be empty",
    "swiftCode": "Swift code cannot be empty",
    "bankName": "Bank name cannot be empty",
    "countryName": "Country cannot be empty",
    "isHeadquarter": "isHeadquarter cannot be empty"
  }
}
```
**Status:** `400 Bad Request`

Validation is handled automatically by `@Valid` annotations in DTO classes and reported via the global exception handler.

---

## 5. Testing

The application includes a full suite of tests to ensure correctness and stability.

### 5.1 Unit Tests
- Test individual components such as:
    - `SwiftCodeService`
    - `SwiftController`
    - `SwiftCodeParser`
- Use of Mockito for mocking dependencies
- Asserts all expected behaviors, success paths and exceptions

### 5.2 Controller Validation Tests
- Uses MockMvc to test endpoint input validation
- Ensures appropriate messages are returned for invalid payloads

### 5.3 Integration Tests
- Test interaction between controller and service layers (without real DB)
- Mocks repository layer
- Validates correct HTTP responses and mappings

### Running All Tests:
```
./mvnw test
```
or
```
mvn test
```

All tests are located under `src/test/java/` and cover both positive and negative scenarios.

---

