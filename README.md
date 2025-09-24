# SMHI Weather Observations Backend

A Spring Boot REST API that fetches and merges weather observation data from SMHI (Swedish Meteorological and Hydrological Institute). The application provides endpoints to retrieve meteorological data including temperature, wind speed, and wind gusts from weather stations across Sweden.

## 🚀 Features

- **Weather Data Integration**: Fetches real-time weather observations from SMHI's Open Data API
- **Data Merging**: Combines multiple weather parameters (temperature, wind gusts, wind speed) into unified observation points
- **Geographic Filtering**: Filter observations by location using latitude/longitude coordinates and radius
- **Time-based Queries**: Retrieve data for the last hour or last day
- **Station Management**: List and query specific weather stations
- **API Security**: Protected endpoints with API key authentication
- **OpenAPI Documentation**: Interactive Swagger UI for API exploration
- **Comprehensive Testing**: Unit tests, integration tests, and web layer tests

## 🏗️ Architecture

### Core Components

- **`SmhiBackendApplication`**: Main Spring Boot application entry point
- **`ObservationsController`**: REST API controller handling HTTP requests
- **`ObservationService`**: Business logic for data processing and merging
- **`SmhiClient`**: HTTP client for SMHI API integration
- **`ApiKeyFilter`**: Security filter for API key validation

### Data Models

- **`ObservationPoint`**: Unified data structure containing station info, timestamp, and weather measurements
- **`StationDto`**: Weather station metadata (ID, name, coordinates)
- **`SmhiClient.SmhiSeries`**: Raw SMHI API response structure
- **`SmhiClient.ValuePoint`**: Individual measurement data point

## 📁 Project Structure

```
smhi/
├── backend/                          # Spring Boot backend application
│   ├── src/main/java/backend/        # Main source code
│   │   ├── SmhiBackendApplication.java    # Application entry point
│   │   ├── ObservationsController.java    # REST API controller
│   │   ├── ObservationService.java        # Business logic service
│   │   ├── SmhiClient.java               # SMHI API client
│   │   ├── SmhiConfig.java               # WebClient configuration
│   │   ├── ApiKeyFilter.java             # Security filter
│   │   ├── ObservationPoint.java         # Data model for observations
│   │   └── StationDto.java               # Data model for stations
│   ├── src/main/resources/           # Configuration files
│   │   └── application.yml           # Application configuration
│   ├── src/test/java/backend/        # Test source code
│   │   ├── ObservationsControllerWebTest.java  # Web layer tests
│   │   ├── ObservationServiceMergeTest.java    # Service logic tests
│   │   ├── ObservationsIntegrationTest.java    # Integration tests
│   │   └── TestConfig.java                   # Test configuration
│   ├── src/test/resources/           # Test configuration
│   │   └── application-test.yml      # Test-specific configuration
│   ├── pom.xml                       # Maven dependencies and build config
│   └── target/                       # Compiled classes and JAR files
├── frontend/                         # Frontend directory (empty)
├── Backend_case_-_utskick.pdf        # Project requirements document
└── README.md                         # This documentation file
```

## 🔧 Configuration

### Application Properties (`application.yml`)

```yaml
server:
  port: 8080

smhi:
  baseUrl: https://opendata-download-metobs.smhi.se/api/version/1.0
  cacheSeconds: 60          # Cache duration to avoid excessive API calls
  stationCacheHours: 24     # Station metadata cache duration
  defaultStations: ["159880"]  # Default station IDs

security:
  apiKey: fhn1CaoXEI6gfOVOuHbJbAbFiM8MY4PH  # API key for authentication

spring:
  cache:
    type: caffeine  # In-memory caching
```

### Maven Dependencies (`pom.xml`)

Key dependencies include:
- **Spring Boot 3.3.3**: Web framework with Java 21 support
- **Spring WebFlux**: Reactive HTTP client for SMHI API calls
- **Lombok**: Reduces boilerplate code
- **Caffeine**: High-performance caching
- **SpringDoc OpenAPI**: API documentation and Swagger UI
- **WireMock**: HTTP service mocking for integration tests
- **AssertJ**: Fluent assertions for testing

## 🌐 API Endpoints

### Base URL
```
http://localhost:8080/api
```

### Authentication
All endpoints (except Swagger UI and health checks) require an API key in the request header:
```
x-api-key: fhn1CaoXEI6gfOVOuHbJbAbFiM8MY4PH
```

### Available Endpoints

#### 1. Get Weather Stations
```http
GET /api/stations?set=core
```

**Parameters:**
- `set` (optional): Station set type (`core`, `additional`, `all`)

**Response:** List of weather stations with metadata

#### 2. Get Weather Observations
```http
GET /api/observations?stationId=159880&range=last-hour&lat=59.3&lon=18.1&radiusKm=50
```

**Parameters:**
- `stationId` (optional): Comma-separated station IDs
- `range` (optional): Time range (`last-hour`, `last-day`) - defaults to `last-hour`
- `from` (optional): Start timestamp (ISO 8601)
- `to` (optional): End timestamp (ISO 8601)
- `lat` (optional): Latitude for geographic filtering
- `lon` (optional): Longitude for geographic filtering
- `radiusKm` (optional): Search radius in kilometers

**Response:** List of merged observation points

### Example Response

```json
[
  {
    "stationId": "159880",
    "stationName": "Stockholm A",
    "lat": 59.3,
    "lon": 18.1,
    "timestampUtc": "2025-01-23T10:00:00Z",
    "gustMs": 12.4,
    "airTempC": 17.1,
    "windSpeedMs": 5.6
  }
]
```

## 🧪 Testing

The project includes comprehensive test coverage:

### Test Types

1. **Unit Tests** (`ObservationServiceMergeTest.java`)
   - Tests data merging logic
   - Validates timestamp handling and null value preservation

2. **Web Layer Tests** (`ObservationsControllerWebTest.java`)
   - Tests REST controller endpoints
   - Validates API key authentication
   - Tests parameter handling

3. **Integration Tests** (`ObservationsIntegrationTest.java`)
   - End-to-end testing with WireMock
   - Tests complete request/response cycle
   - Validates SMHI API integration

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=ObservationsControllerWebTest

# Run with coverage
mvn test jacoco:report
```

## 🚀 Getting Started

### Prerequisites

- Java 21 or higher
- Maven 3.6 or higher
- Internet connection (for SMHI API access)

### Installation & Running

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd smhi
   ```

2. **Build the application**
   ```bash
   cd backend
   mvn clean install
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

4. **Access the application**
   - API Base URL: `http://localhost:8080/api`
   - Swagger UI: `http://localhost:8080/swagger-ui.html`
   - Health Check: `http://localhost:8080/actuator/health`

### Example API Calls

```bash
# Get all stations
curl -H "x-api-key: fhn1CaoXEI6gfOVOuHbJbAbFiM8MY4PH" \
     http://localhost:8080/api/stations

# Get latest hour observations for Stockholm
curl -H "x-api-key: fhn1CaoXEI6gfOVOuHbJbAbFiM8MY4PH" \
     "http://localhost:8080/api/observations?stationId=159880&range=last-hour"

# Get observations within 50km of Stockholm
curl -H "x-api-key: fhn1CaoXEI6gfOVOuHbJbAbFiM8MY4PH" \
     "http://localhost:8080/api/observations?lat=59.3&lon=18.1&radiusKm=50"
```

## 📊 Data Sources

The application integrates with SMHI's Open Data API:

- **Base URL**: `https://opendata-download-metobs.smhi.se/api/version/1.0`
- **Parameters**:
  - Parameter 1: Air Temperature (Lufttemperatur)
  - Parameter 4: Wind Speed (Vindhastighet)
  - Parameter 21: Wind Gust (Byvind)

## 🔒 Security

- **API Key Authentication**: All endpoints require a valid API key
- **Public Endpoints**: Swagger UI, OpenAPI docs, health checks, and favicon are publicly accessible
- **CORS Support**: Preflight OPTIONS requests are allowed

## 🎯 Key Features Implementation

### Data Merging Logic
The `ObservationService` merges multiple weather parameters by:
1. Fetching data for each parameter (temperature, wind gusts, wind speed)
2. Creating a union of all timestamps
3. Combining measurements into unified `ObservationPoint` objects
4. Preserving null values for missing measurements

### Geographic Filtering
Uses the Haversine formula to calculate distances between coordinates and filter observations within a specified radius.

### Caching Strategy
- **Station Metadata**: Cached for 24 hours (stable data)
- **Observation Data**: Cached for 60 seconds (frequently changing)

## 🛠️ Development

### Code Style
- Java 21 with modern features (records, text blocks)
- Spring Boot best practices
- Comprehensive error handling
- Extensive logging for debugging

### Extensibility
The architecture supports easy extension for:
- Additional weather parameters
- New data sources
- Enhanced filtering options
- Custom caching strategies

## 📝 License

This project is part of a technical assignment for SMHI backend development.

## 🤝 Contributing

This is a technical assessment project. For questions or issues, please refer to the project requirements document (`Backend_case_-_utskick.pdf`).

---

**Note**: The frontend directory is currently empty and not part of this backend implementation.