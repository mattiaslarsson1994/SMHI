# File Documentation Index

This document provides a comprehensive index of all files in the SMHI Weather Observations Backend project with their purposes and key information.

## Project Root Files

### README.md
- **Purpose**: Main project documentation and getting started guide
- **Content**: Project overview, features, installation instructions, API documentation, examples
- **Status**: ✅ Updated with comprehensive documentation

### Backend_case_-_utskick.pdf
- **Purpose**: Project requirements and specifications document
- **Content**: Technical requirements, API specifications, implementation guidelines
- **Status**: 📄 Reference document (not editable)

## Backend Source Files

### Core Application Files

#### SmhiBackendApplication.java
- **Location**: `backend/src/main/java/backend/SmhiBackendApplication.java`
- **Purpose**: Main Spring Boot application entry point
- **Key Features**: 
  - `@SpringBootApplication` annotation
  - Main method for application startup
  - Auto-configuration setup
- **Dependencies**: Spring Boot framework
- **Documentation**: ✅ Documented in `docs/backend-source-files.md`

#### ObservationsController.java
- **Location**: `backend/src/main/java/backend/ObservationsController.java`
- **Purpose**: REST API controller for weather observations
- **Key Features**:
  - Two main endpoints: `/api/stations` and `/api/observations`
  - OpenAPI documentation with Swagger
  - Parameter validation and binding
  - Constructor injection with Lombok
- **Endpoints**:
  - `GET /api/stations` - List weather stations
  - `GET /api/observations` - Get merged weather observations
- **Dependencies**: Spring Web, OpenAPI, Lombok
- **Documentation**: ✅ Documented in `docs/backend-source-files.md`

#### ObservationService.java
- **Location**: `backend/src/main/java/backend/ObservationService.java`
- **Purpose**: Core business logic for data processing and merging
- **Key Features**:
  - Merges multiple weather parameters (temperature, wind gusts, wind speed)
  - Geographic filtering using Haversine distance calculation
  - Time-based filtering and station resolution
  - Caching integration
- **Main Methods**:
  - `getMergedObservations()` - Main data processing method
  - `getStations()` - Station metadata retrieval
  - `resolveStations()` - Station ID resolution
- **Dependencies**: SmhiClient, Spring configuration
- **Documentation**: ✅ Documented in `docs/backend-source-files.md`

#### SmhiClient.java
- **Location**: `backend/src/main/java/backend/SmhiClient.java`
- **Purpose**: HTTP client for SMHI API integration
- **Key Features**:
  - Reactive HTTP client using WebClient
  - Fetches data for specific weather parameters
  - Handles both hourly and daily data requests
  - JSON parsing and error handling
- **Main Methods**:
  - `fetchLatestHour()` - Get latest hour data
  - `fetchLatestDay()` - Get latest day data
  - `fetchStationsFromParameter()` - Get station metadata
- **Inner Classes**:
  - `SmhiSeries` - API response structure
  - `ValuePoint` - Individual measurement data
- **Dependencies**: Spring WebFlux, Jackson, Reactor
- **Documentation**: ✅ Documented in `docs/backend-source-files.md`

#### SmhiConfig.java
- **Location**: `backend/src/main/java/backend/SmhiConfig.java`
- **Purpose**: Configuration for HTTP client and external integrations
- **Key Features**:
  - WebClient bean configuration
  - Connection timeout and compression settings
  - Memory buffer configuration
  - Base URL configuration
- **Configuration**:
  - Response timeout: 15 seconds
  - Connection timeout: 5 seconds
  - Max in-memory size: 8 MB
  - Compression enabled
- **Dependencies**: Spring WebFlux, Reactor Netty
- **Documentation**: ✅ Documented in `docs/backend-source-files.md`

#### ApiKeyFilter.java
- **Location**: `backend/src/main/java/backend/ApiKeyFilter.java`
- **Purpose**: Security filter for API key authentication
- **Key Features**:
  - Validates `x-api-key` header
  - Allows public access to documentation endpoints
  - Handles CORS preflight requests
  - Returns JSON error responses
- **Public Endpoints**:
  - Swagger UI (`/swagger-ui/**`)
  - OpenAPI docs (`/v3/api-docs/**`)
  - Health checks (`/actuator/health`)
  - Favicon and OPTIONS requests
- **Dependencies**: Jakarta Servlet API, Spring framework
- **Documentation**: ✅ Documented in `docs/backend-source-files.md`

### Data Model Files

#### ObservationPoint.java
- **Location**: `backend/src/main/java/backend/ObservationPoint.java`
- **Purpose**: Immutable data record for unified weather observations
- **Fields**:
  - `stationId` - Station identifier
  - `stationName` - Human-readable station name
  - `lat`, `lon` - Geographic coordinates
  - `timestampUtc` - UTC timestamp
  - `gustMs` - Wind gust speed (nullable)
  - `airTempC` - Air temperature (nullable)
  - `windSpeedMs` - Wind speed (nullable)
- **Key Features**: Java record, null-safe measurements, unified format
- **Documentation**: ✅ Documented in `docs/backend-source-files.md`

#### StationDto.java
- **Location**: `backend/src/main/java/backend/StationDto.java`
- **Purpose**: Data transfer object for weather station metadata
- **Fields**:
  - `id` - Station identifier
  - `name` - Station name
  - `lat`, `lon` - Geographic coordinates
- **Key Features**: Java record, minimal structure, API response format
- **Documentation**: ✅ Documented in `docs/backend-source-files.md`

## Configuration Files

### Maven Configuration

#### pom.xml
- **Location**: `backend/pom.xml`
- **Purpose**: Maven project configuration and dependency management
- **Key Dependencies**:
  - Spring Boot 3.3.3 with Java 21
  - Spring Web and WebFlux
  - Lombok for boilerplate reduction
  - Caffeine for caching
  - SpringDoc OpenAPI for documentation
  - WireMock for testing
  - AssertJ for assertions
- **Build Configuration**: JAR packaging, Spring Boot plugin
- **Documentation**: ✅ Documented in `docs/backend-source-files.md`

### Application Configuration

#### application.yml
- **Location**: `backend/src/main/resources/application.yml`
- **Purpose**: Main application configuration
- **Configuration Sections**:
  - Server: Port 8080
  - SMHI: Base URL, cache settings, default stations
  - Security: API key configuration
  - Spring: Caffeine caching
- **Key Settings**:
  - SMHI base URL: `https://opendata-download-metobs.smhi.se/api/version/1.0`
  - Cache: 60 seconds for observations, 24 hours for stations
  - Default station: Stockholm A (ID: 159880)
- **Documentation**: ✅ Documented in `docs/backend-source-files.md`

#### application-test.yml
- **Location**: `backend/src/test/resources/application-test.yml`
- **Purpose**: Test-specific configuration overrides
- **Configuration**:
  - API key: "test-key" for testing
  - Minimal overrides for test environment
- **Documentation**: ✅ Documented in `docs/backend-source-files.md`

## Test Files

### Unit Tests

#### ObservationServiceMergeTest.java
- **Location**: `backend/src/test/java/backend/ObservationServiceMergeTest.java`
- **Purpose**: Unit tests for data merging logic
- **Test Coverage**:
  - Data merging by timestamp
  - Null value preservation
  - Multiple parameter handling
- **Key Features**:
  - Pure unit test (no Spring context)
  - Realistic test fixtures
  - AssertJ assertions
- **Documentation**: ✅ Documented in `docs/backend-source-files.md`

### Integration Tests

#### ObservationsIntegrationTest.java
- **Location**: `backend/src/test/java/backend/ObservationsIntegrationTest.java`
- **Purpose**: End-to-end integration tests
- **Test Coverage**:
  - Complete request/response cycle
  - SMHI API integration with WireMock
  - Different time ranges (last-hour, last-day)
  - Default parameter handling
- **Key Features**:
  - Full Spring Boot context
  - WireMock for SMHI API mocking
  - Dynamic property overrides
  - WebClient testing
- **Documentation**: ✅ Documented in `docs/backend-source-files.md`

### Web Layer Tests

#### ObservationsControllerWebTest.java
- **Location**: `backend/src/test/java/backend/ObservationsControllerWebTest.java`
- **Purpose**: Web layer tests for REST controller
- **Test Coverage**:
  - API key authentication
  - Endpoint parameter handling
  - JSON response validation
  - Error response testing
- **Key Features**:
  - MockMvc for HTTP simulation
  - Service mocking with Mockito
  - Web layer test slice
  - Test profile activation
- **Documentation**: ✅ Documented in `docs/backend-source-files.md`

### Test Configuration

#### TestConfig.java
- **Location**: `backend/src/test/java/backend/TestConfig.java`
- **Purpose**: Test-specific configuration and beans
- **Key Features**:
  - Test configuration annotation
  - Mock service implementation
  - Primary bean override
  - Predictable test data
- **Documentation**: ✅ Documented in `docs/backend-source-files.md`

## Build and Target Files

### Compiled Classes
- **Location**: `backend/target/classes/`
- **Purpose**: Compiled Java classes and resources
- **Content**: All compiled `.class` files and configuration resources
- **Status**: 🔄 Generated during build process

### Test Classes
- **Location**: `backend/target/test-classes/`
- **Purpose**: Compiled test classes and test resources
- **Content**: All compiled test `.class` files and test configuration
- **Status**: 🔄 Generated during test compilation

### JAR Files
- **Location**: `backend/target/`
- **Files**:
  - `backend-0.0.1-SNAPSHOT.jar` - Executable JAR
  - `backend-0.0.1-SNAPSHOT.jar.original` - Original JAR without dependencies
- **Purpose**: Deployable application artifacts
- **Status**: 🔄 Generated during Maven package phase

### Test Reports
- **Location**: `backend/target/surefire-reports/`
- **Purpose**: Test execution reports
- **Content**: Test results, coverage reports, XML summaries
- **Status**: 🔄 Generated during test execution

## Documentation Files

### Project Documentation

#### docs/backend-source-files.md
- **Purpose**: Detailed documentation of all backend source files
- **Content**: Component descriptions, method documentation, dependencies
- **Status**: ✅ Complete

#### docs/api-documentation.md
- **Purpose**: Comprehensive API documentation
- **Content**: Endpoint descriptions, request/response examples, error handling
- **Status**: ✅ Complete

#### docs/development-guide.md
- **Purpose**: Development setup and best practices guide
- **Content**: Setup instructions, testing strategies, deployment guide
- **Status**: ✅ Complete

#### docs/architecture-overview.md
- **Purpose**: System architecture and design documentation
- **Content**: Component diagrams, data flow, technology stack
- **Status**: ✅ Complete

#### docs/file-index.md
- **Purpose**: This file - comprehensive index of all project files
- **Content**: File locations, purposes, documentation status
- **Status**: ✅ Complete

## Frontend Directory

### frontend/
- **Location**: `frontend/`
- **Purpose**: Frontend application directory
- **Content**: Currently empty
- **Status**: 📁 Empty directory (not part of current implementation)

## File Status Legend

- ✅ **Complete**: Fully documented and implemented
- 🔄 **Generated**: Automatically generated during build process
- 📄 **Reference**: External reference document
- 📁 **Empty**: Directory or file not yet implemented
- ⚠️ **Needs Attention**: Requires updates or fixes

## Summary

The SMHI Weather Observations Backend project contains:

- **8 Java source files** (core application logic)
- **2 data model files** (immutable records)
- **3 configuration files** (Maven and application config)
- **4 test files** (comprehensive test coverage)
- **5 documentation files** (complete project documentation)
- **Multiple build artifacts** (compiled classes, JAR files, test reports)

All source files are fully documented with their purposes, key features, dependencies, and implementation details. The project follows Spring Boot best practices with comprehensive testing and clear separation of concerns.
