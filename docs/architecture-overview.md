# Architecture Overview

## System Architecture

The SMHI Weather Observations Backend follows a layered architecture pattern with clear separation of concerns, designed for scalability, maintainability, and testability.

```
┌─────────────────────────────────────────────────────────────┐
│                    Client Layer                             │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐        │
│  │   Web UI    │  │  Mobile App │  │  API Client │        │
│  └─────────────┘  └─────────────┘  └─────────────┘        │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                 Presentation Layer                          │
│  ┌─────────────────────────────────────────────────────────┐│
│  │              ObservationsController                     ││
│  │  • REST API endpoints                                  ││
│  │  • Request/Response handling                           ││
│  │  • Parameter validation                               ││
│  │  • OpenAPI documentation                              ││
│  └─────────────────────────────────────────────────────────┘│
│  ┌─────────────────────────────────────────────────────────┐│
│  │                ApiKeyFilter                            ││
│  │  • Authentication & Authorization                      ││
│  │  • Request filtering                                  ││
│  │  • Security enforcement                               ││
│  └─────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                  Business Layer                             │
│  ┌─────────────────────────────────────────────────────────┐│
│  │              ObservationService                         ││
│  │  • Data merging logic                                  ││
│  │  • Business rules                                      ││
│  │  • Geographic filtering                                ││
│  │  • Time-based filtering                                ││
│  │  • Station resolution                                  ││
│  └─────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                  Integration Layer                          │
│  ┌─────────────────────────────────────────────────────────┐│
│  │                SmhiClient                              ││
│  │  • HTTP client for SMHI API                           ││
│  │  • Reactive programming                               ││
│  │  • JSON parsing                                       ││
│  │  • Error handling                                     ││
│  └─────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                  External Systems                           │
│  ┌─────────────────────────────────────────────────────────┐│
│  │                SMHI Open Data API                      ││
│  │  • Weather observations                                ││
│  │  • Station metadata                                    ││
│  │  • Real-time data                                      ││
│  └─────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────┘
```

## Component Architecture

### Core Components

#### 1. SmhiBackendApplication
- **Role**: Application bootstrap and configuration
- **Responsibilities**:
  - Spring Boot application initialization
  - Auto-configuration setup
  - Component scanning

#### 2. ObservationsController
- **Role**: REST API endpoint handler
- **Responsibilities**:
  - HTTP request/response handling
  - Parameter validation and binding
  - JSON serialization/deserialization
  - OpenAPI documentation
  - Error response formatting

#### 3. ObservationService
- **Role**: Business logic orchestrator
- **Responsibilities**:
  - Data merging and transformation
  - Geographic filtering (Haversine distance)
  - Time-based filtering
  - Station resolution and metadata enrichment
  - Caching coordination

#### 4. SmhiClient
- **Role**: External API integration
- **Responsibilities**:
  - HTTP communication with SMHI API
  - Reactive programming with WebClient
  - JSON response parsing
  - Error handling and retry logic
  - Connection pooling and timeouts

#### 5. ApiKeyFilter
- **Role**: Security enforcement
- **Responsibilities**:
  - API key validation
  - Request filtering
  - Public endpoint bypass
  - CORS preflight handling

### Data Models

#### 1. ObservationPoint
```java
public record ObservationPoint(
  String stationId,      // Station identifier
  String stationName,    // Human-readable name
  double lat,           // Latitude coordinate
  double lon,           // Longitude coordinate
  Instant timestampUtc, // UTC timestamp
  Double gustMs,        // Wind gust (m/s) - nullable
  Double airTempC,      // Air temperature (°C) - nullable
  Double windSpeedMs    // Wind speed (m/s) - nullable
)
```

#### 2. StationDto
```java
public record StationDto(
  String id,    // Station identifier
  String name,  // Station name
  double lat,   // Latitude
  double lon    // Longitude
)
```

## Data Flow Architecture

### Request Processing Flow

```
1. HTTP Request
   ↓
2. ApiKeyFilter (Authentication)
   ↓
3. ObservationsController (Routing & Validation)
   ↓
4. ObservationService (Business Logic)
   ↓
5. SmhiClient (External API Calls)
   ↓
6. SMHI API (Data Source)
   ↓
7. Data Processing & Merging
   ↓
8. Response Serialization
   ↓
9. HTTP Response
```

### Data Merging Process

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Temperature   │    │   Wind Gusts    │    │   Wind Speed    │
│   (Param 1)     │    │   (Param 21)    │    │   (Param 4)     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Timestamp     │    │   Timestamp     │    │   Timestamp     │
│   Value Maps    │    │   Value Maps    │    │   Value Maps    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 ▼
                    ┌─────────────────────────┐
                    │   Union of Timestamps   │
                    │   (TreeSet<Long>)       │
                    └─────────────────────────┘
                                 │
                                 ▼
                    ┌─────────────────────────┐
                    │   Merged Observations   │
                    │   (ObservationPoint[])  │
                    └─────────────────────────┘
```

## Technology Stack

### Core Framework
- **Spring Boot 3.3.3**: Application framework
- **Java 21**: Programming language with modern features
- **Maven**: Build and dependency management

### Web Layer
- **Spring Web**: REST API framework
- **Spring WebFlux**: Reactive HTTP client
- **Jackson**: JSON processing
- **SpringDoc OpenAPI**: API documentation

### Data Processing
- **Reactor**: Reactive programming
- **Caffeine**: High-performance caching
- **Java Streams**: Functional data processing

### Testing
- **JUnit 5**: Testing framework
- **Mockito**: Mocking framework
- **WireMock**: HTTP service mocking
- **AssertJ**: Fluent assertions
- **Spring Boot Test**: Integration testing

### Security
- **Jakarta Servlet**: Filter implementation
- **Spring Security**: Security framework (implicit)

## Configuration Architecture

### Configuration Hierarchy

```
1. application.yml (Base configuration)
   ↓
2. application-{profile}.yml (Profile-specific)
   ↓
3. Environment variables (Runtime overrides)
   ↓
4. Command line arguments (Final overrides)
```

### Configuration Components

#### 1. SmhiConfig
- **Purpose**: HTTP client configuration
- **Responsibilities**:
  - WebClient bean creation
  - Connection timeout configuration
  - Memory buffer settings
  - Compression settings

#### 2. Application Properties
- **Server Configuration**: Port, context path
- **SMHI Integration**: Base URL, cache settings
- **Security**: API key configuration
- **Spring Configuration**: Caching, profiles

## Caching Architecture

### Cache Strategy

```
┌─────────────────────────────────────────────────────────────┐
│                    Cache Layers                             │
│                                                             │
│  ┌─────────────────┐    ┌─────────────────┐                │
│  │  Station Cache  │    │ Observation     │                │
│  │  (24 hours)     │    │ Cache           │                │
│  │                 │    │ (60 seconds)    │                │
│  │ • Station IDs   │    │                 │                │
│  │ • Names         │    │ • Temperature   │                │
│  │ • Coordinates   │    │ • Wind data     │                │
│  │ • Metadata      │    │ • Timestamps    │                │
│  └─────────────────┘    └─────────────────┘                │
└─────────────────────────────────────────────────────────────┘
```

### Cache Implementation
- **Caffeine**: In-memory cache implementation
- **Spring Cache**: Abstraction layer
- **TTL-based Expiration**: Time-based cache invalidation
- **Size-based Eviction**: Memory-based cache management

## Security Architecture

### Authentication Flow

```
1. HTTP Request with x-api-key header
   ↓
2. ApiKeyFilter.doFilter()
   ↓
3. Check if path is public
   ↓
4. Validate API key against configured value
   ↓
5. Allow/Deny request
```

### Security Components

#### 1. ApiKeyFilter
- **Order**: 1 (highest priority)
- **Scope**: All non-public endpoints
- **Validation**: String comparison with configured key

#### 2. Public Endpoints
- Swagger UI (`/swagger-ui/**`)
- OpenAPI docs (`/v3/api-docs/**`)
- Health checks (`/actuator/health`)
- Favicon (`/favicon.ico`)
- CORS preflight (`OPTIONS` requests)

## Error Handling Architecture

### Error Response Flow

```
1. Exception occurs in any layer
   ↓
2. Spring Boot error handling
   ↓
3. Error response formatting
   ↓
4. HTTP error response
```

### Error Types

#### 1. Authentication Errors (401)
- Missing API key
- Invalid API key
- Malformed headers

#### 2. Validation Errors (400)
- Invalid parameters
- Malformed requests
- Type conversion errors

#### 3. Server Errors (500)
- SMHI API unavailability
- Network timeouts
- Internal processing errors

## Performance Architecture

### Optimization Strategies

#### 1. HTTP Client Optimization
- **Connection Pooling**: Reuse HTTP connections
- **Compression**: Gzip compression for responses
- **Timeouts**: Configurable connection and response timeouts
- **Memory Management**: Bounded buffer sizes

#### 2. Caching Strategy
- **Station Metadata**: Long-term caching (24 hours)
- **Observation Data**: Short-term caching (60 seconds)
- **In-Memory Storage**: Fast access with Caffeine

#### 3. Data Processing
- **Stream Processing**: Lazy evaluation and memory efficiency
- **Parallel Processing**: Concurrent API calls where possible
- **Batch Operations**: Efficient data merging

## Scalability Considerations

### Horizontal Scaling
- **Stateless Design**: No server-side session state
- **External Configuration**: Environment-based configuration
- **Load Balancer Ready**: Health check endpoints

### Vertical Scaling
- **Memory Management**: Efficient data structures
- **CPU Optimization**: Stream processing and caching
- **I/O Optimization**: Reactive programming and connection pooling

### Monitoring and Observability
- **Health Checks**: Application and dependency health
- **Metrics**: Performance and usage metrics
- **Logging**: Structured logging for debugging
- **Tracing**: Request/response tracing capabilities

## Deployment Architecture

### Container Deployment
```
┌─────────────────────────────────────────────────────────────┐
│                    Docker Container                         │
│                                                             │
│  ┌─────────────────────────────────────────────────────────┐│
│  │                JVM (Java 21)                           ││
│  │                                                         ││
│  │  ┌─────────────────────────────────────────────────────┐││
│  │  │            Spring Boot Application                  │││
│  │  │                                                     │││
│  │  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐ │││
│  │  │  │ Controllers │  │  Services   │  │   Clients   │ │││
│  │  │  └─────────────┘  └─────────────┘  └─────────────┘ │││
│  │  └─────────────────────────────────────────────────────┘││
│  └─────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────┘
```

### Environment Configuration
- **Development**: Local development with test data
- **Testing**: Integration testing with WireMock
- **Production**: Live SMHI API integration

This architecture provides a solid foundation for a scalable, maintainable, and testable weather data service that can handle real-time data processing and serve multiple client applications.
