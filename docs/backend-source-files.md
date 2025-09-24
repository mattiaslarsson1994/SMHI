# Backend Source Files Documentation

## Core Application Files

### SmhiBackendApplication.java
**Location**: `backend/src/main/java/backend/SmhiBackendApplication.java`

**Purpose**: Main Spring Boot application entry point that bootstraps the application.

**Key Features**:
- Standard Spring Boot application class
- Uses `@SpringBootApplication` annotation for auto-configuration
- Contains the main method to run the application

**Dependencies**: Spring Boot framework

---

### ObservationsController.java
**Location**: `backend/src/main/java/backend/ObservationsController.java`

**Purpose**: REST API controller that handles HTTP requests for weather observations and station data.

**Key Features**:
- **`@RestController`**: Marks as REST controller
- **`@RequestMapping("/api")`**: Base path for all endpoints
- **`@Tag(name = "Observations")`**: OpenAPI documentation tag
- **`@RequiredArgsConstructor`**: Lombok annotation for constructor injection

**Endpoints**:
1. **`GET /api/stations`**
   - Returns list of weather stations
   - Parameter: `set` (default: "core") - station set type
   - Response: `List<StationDto>`

2. **`GET /api/observations`**
   - Returns merged weather observations
   - Parameters:
     - `stationId` (optional): Comma-separated station IDs
     - `range` (optional): Time range ("last-hour" or "last-day")
     - `from` (optional): Start timestamp
     - `to` (optional): End timestamp
     - `lat` (optional): Latitude for geographic filtering
     - `lon` (optional): Longitude for geographic filtering
     - `radiusKm` (optional): Search radius in kilometers
   - Response: `List<ObservationPoint>`

**Dependencies**: 
- `ObservationService` (injected)
- Spring Web annotations
- OpenAPI annotations for documentation

---

### ObservationService.java
**Location**: `backend/src/main/java/backend/ObservationService.java`

**Purpose**: Core business logic service that processes and merges weather observation data from multiple SMHI parameters.

**Key Features**:
- **Data Merging**: Combines temperature, wind gusts, and wind speed data
- **Station Resolution**: Handles station ID resolution and metadata enrichment
- **Geographic Filtering**: Implements Haversine distance calculation
- **Time-based Filtering**: Supports custom time ranges
- **Caching Integration**: Works with Spring's caching framework

**Main Methods**:

1. **`getMergedObservations()`**
   - Fetches data for multiple parameters (temperature, wind gusts, wind speed)
   - Merges data by timestamp across all parameters
   - Applies time and geographic filters
   - Returns sorted list of observation points

2. **`getStations()`**
   - Returns list of available weather stations
   - Fetches station metadata from SMHI API

3. **`resolveStations()`**
   - Resolves station IDs to station metadata
   - Handles comma-separated station ID lists
   - Falls back to all stations if no specific IDs provided

**Helper Methods**:
- **`toMap()`**: Converts SMHI series data to timestamp-value maps
- **`haversineKm()`**: Calculates distance between two geographic points

**Dependencies**:
- `SmhiClient` (injected)
- Spring `@Value` for configuration
- Java 21 features (records, streams, text blocks)

---

### SmhiClient.java
**Location**: `backend/src/main/java/backend/SmhiClient.java`

**Purpose**: HTTP client component that communicates with SMHI's Open Data API to fetch weather observation data.

**Key Features**:
- **Reactive HTTP Client**: Uses Spring WebFlux WebClient
- **Parameter-based Queries**: Fetches data for specific weather parameters
- **Time Period Support**: Handles both hourly and daily data requests
- **Station Metadata**: Retrieves station information and coordinates
- **JSON Parsing**: Handles SMHI's JSON response format

**Main Methods**:

1. **`fetchLatestHour(stationId, parameterId)`**
   - Fetches latest hour of data for a specific station and parameter
   - Returns `SmhiSeries` with value points

2. **`fetchLatestDay(stationId, parameterId)`**
   - Fetches latest day of data for a specific station and parameter
   - Returns `SmhiSeries` with value points

3. **`fetchStationsFromParameter(parameterId)`**
   - Fetches all stations that provide data for a specific parameter
   - Returns `List<StationDto>` with station metadata

**Inner Classes**:

1. **`SmhiSeries`**
   - POJO for SMHI API response
   - Contains list of `ValuePoint` objects

2. **`ValuePoint`**
   - Individual measurement data point
   - Fields: `date` (epoch milliseconds), `value` (measurement)
   - Helper method: `asInstantUtc()` for timestamp conversion

**Dependencies**:
- Spring WebFlux WebClient
- Jackson for JSON processing
- Reactor for reactive programming

---

### SmhiConfig.java
**Location**: `backend/src/main/java/backend/SmhiConfig.java`

**Purpose**: Configuration class that sets up the WebClient for SMHI API communication with optimized settings.

**Key Features**:
- **WebClient Bean**: Configures HTTP client with specific settings
- **Connection Optimization**: Sets timeouts and compression
- **Memory Management**: Configures buffer sizes for large responses
- **Base URL Configuration**: Uses configurable SMHI API base URL

**Configuration Details**:
- **Compression**: Enabled for gzip support
- **Response Timeout**: 15 seconds
- **Connection Timeout**: 5 seconds
- **Max In-Memory Size**: 8 MB for large JSON responses
- **Base URL**: Configurable via `smhi.baseUrl` property

**Dependencies**:
- Spring WebFlux
- Reactor Netty HTTP client
- Spring configuration annotations

---

### ApiKeyFilter.java
**Location**: `backend/src/main/java/backend/ApiKeyFilter.java`

**Purpose**: Security filter that validates API keys for protected endpoints while allowing public access to documentation and health checks.

**Key Features**:
- **API Key Validation**: Checks `x-api-key` header
- **Public Endpoints**: Allows access to Swagger UI, OpenAPI docs, health checks
- **CORS Support**: Handles preflight OPTIONS requests
- **Error Responses**: Returns JSON error messages for unauthorized access

**Public Endpoints**:
- `/swagger-ui.html` and `/swagger-ui/*`
- `/v3/api-docs` and `/v3/api-docs/*`
- `/favicon.ico`
- `/actuator/health`
- All OPTIONS requests (CORS preflight)

**Security Logic**:
1. Checks if request path is public
2. Validates API key against configured value
3. Returns 401 Unauthorized for invalid/missing keys
4. Allows request to proceed if valid

**Dependencies**:
- Jakarta Servlet API
- Spring framework annotations
- Spring `@Value` for configuration

---

## Data Model Files

### ObservationPoint.java
**Location**: `backend/src/main/java/backend/ObservationPoint.java`

**Purpose**: Immutable data record representing a unified weather observation point with station information, timestamp, and multiple weather measurements.

**Fields**:
- **`stationId`**: Unique identifier for the weather station
- **`stationName`**: Human-readable station name
- **`lat`**: Latitude coordinate
- **`lon`**: Longitude coordinate
- **`timestampUtc`**: UTC timestamp of the observation
- **`gustMs`**: Wind gust speed in meters per second (can be null)
- **`airTempC`**: Air temperature in Celsius (can be null)
- **`windSpeedMs`**: Wind speed in meters per second (can be null)

**Key Features**:
- **Java Record**: Immutable data structure
- **Null Safety**: Weather measurements can be null if not available
- **Unified Format**: Combines multiple weather parameters into single object
- **Geographic Data**: Includes station coordinates for location-based queries

---

### StationDto.java
**Location**: `backend/src/main/java/backend/StationDto.java`

**Purpose**: Data transfer object representing weather station metadata including identification and geographic coordinates.

**Fields**:
- **`id`**: Unique station identifier
- **`name`**: Station name
- **`lat`**: Latitude coordinate
- **`lon`**: Longitude coordinate

**Key Features**:
- **Java Record**: Immutable data structure
- **Simple Structure**: Minimal data needed for station identification
- **Geographic Coordinates**: Essential for location-based filtering
- **API Response Format**: Matches SMHI API station data structure

---

## Configuration Files

### pom.xml
**Location**: `backend/pom.xml`

**Purpose**: Maven project configuration file defining dependencies, build settings, and project metadata.

**Key Dependencies**:
- **Spring Boot 3.3.3**: Main framework with Java 21 support
- **Spring Web**: REST API support
- **Spring WebFlux**: Reactive HTTP client
- **Lombok**: Reduces boilerplate code
- **Caffeine**: High-performance caching
- **SpringDoc OpenAPI**: API documentation
- **WireMock**: HTTP service mocking for tests
- **AssertJ**: Fluent test assertions

**Build Configuration**:
- **Java Version**: 21
- **Packaging**: JAR
- **Spring Boot Maven Plugin**: For executable JAR creation

---

### application.yml
**Location**: `backend/src/main/resources/application.yml`

**Purpose**: Main application configuration file with environment-specific settings.

**Configuration Sections**:

1. **Server Configuration**:
   - Port: 8080

2. **SMHI Integration**:
   - Base URL: SMHI Open Data API endpoint
   - Cache settings: 60 seconds for observations, 24 hours for stations
   - Default stations: Stockholm A (ID: 159880)

3. **Security**:
   - API key for endpoint authentication

4. **Spring Configuration**:
   - Caffeine caching enabled

---

### application-test.yml
**Location**: `backend/src/test/resources/application-test.yml`

**Purpose**: Test-specific configuration overriding production settings.

**Test Configuration**:
- **API Key**: "test-key" for test authentication
- **Minimal Settings**: Only essential overrides for testing

---

## Test Files

### ObservationsControllerWebTest.java
**Location**: `backend/src/test/java/backend/ObservationsControllerWebTest.java`

**Purpose**: Web layer tests for the REST controller using MockMvc to test HTTP request/response handling.

**Test Coverage**:
1. **Authentication Tests**:
   - Blocks requests without API key
   - Allows requests with valid API key

2. **Endpoint Tests**:
   - Tests default parameter handling
   - Validates range parameter ("last-day")
   - Checks JSON response format

3. **Mocking Strategy**:
   - Mocks `ObservationService` to isolate controller logic
   - Uses `@WebMvcTest` for focused web layer testing

**Key Features**:
- **`@WebMvcTest`**: Spring Boot test slice for web layer
- **`@ActiveProfiles("test")`**: Uses test configuration
- **MockMvc**: HTTP request simulation
- **AssertJ**: Fluent assertions for response validation

---

### ObservationServiceMergeTest.java
**Location**: `backend/src/test/java/backend/ObservationServiceMergeTest.java`

**Purpose**: Unit tests for the data merging logic in the observation service.

**Test Coverage**:
1. **Data Merging Logic**:
   - Tests merging of multiple weather parameters
   - Validates timestamp-based merging
   - Ensures null values are preserved

2. **Test Data**:
   - Uses realistic weather station data
   - Tests with missing data points (null values)
   - Validates proper handling of incomplete datasets

**Key Features**:
- **Pure Unit Test**: No Spring context, tests business logic directly
- **AssertJ**: Fluent assertions for data validation
- **Realistic Fixtures**: Uses actual weather data patterns

---

### ObservationsIntegrationTest.java
**Location**: `backend/src/test/java/backend/ObservationsIntegrationTest.java`

**Purpose**: Integration tests that test the complete application flow from HTTP request to SMHI API integration.

**Test Coverage**:
1. **End-to-End Testing**:
   - Tests complete request/response cycle
   - Validates SMHI API integration
   - Tests with different time ranges

2. **WireMock Integration**:
   - Mocks SMHI API responses
   - Tests with realistic JSON data
   - Validates HTTP client configuration

**Key Features**:
- **`@SpringBootTest`**: Full application context
- **WireMock**: HTTP service mocking
- **Dynamic Properties**: Overrides SMHI base URL for testing
- **WebClient**: Tests actual HTTP client behavior

---

### TestConfig.java
**Location**: `backend/src/test/java/backend/TestConfig.java`

**Purpose**: Test configuration class providing test-specific beans and overrides.

**Key Features**:
- **`@TestConfiguration`**: Test-specific configuration
- **Service Stub**: Provides mock implementation of `ObservationService`
- **`@Primary`**: Overrides production service for testing
- **Test Data**: Returns predictable test data for integration tests

**Dependencies**:
- Spring Test framework
- Test-specific bean definitions
