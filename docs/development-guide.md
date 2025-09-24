# Development Guide

## Project Setup

### Prerequisites

- **Java 21** or higher
- **Maven 3.6** or higher
- **Git** for version control
- **IDE** (IntelliJ IDEA, Eclipse, or VS Code recommended)

### Environment Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd smhi
   ```

2. **Verify Java version**
   ```bash
   java -version
   # Should show Java 21 or higher
   ```

3. **Verify Maven installation**
   ```bash
   mvn -version
   # Should show Maven 3.6 or higher
   ```

## Building and Running

### Development Build

```bash
cd backend
mvn clean compile
```

### Run Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=ObservationsControllerWebTest

# Run with verbose output
mvn test -X

# Run integration tests only
mvn test -Dtest=*IntegrationTest
```

### Run Application

```bash
# Development mode with hot reload
mvn spring-boot:run

# Or build and run JAR
mvn clean package
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

### Application URLs

- **API Base**: `http://localhost:8080/api`
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **Health Check**: `http://localhost:8080/actuator/health`

## Code Structure

### Package Organization

```
backend/src/main/java/backend/
├── SmhiBackendApplication.java    # Main application class
├── ObservationsController.java    # REST API controller
├── ObservationService.java        # Business logic service
├── SmhiClient.java               # SMHI API client
├── SmhiConfig.java               # Configuration beans
├── ApiKeyFilter.java             # Security filter
├── ObservationPoint.java         # Data model
└── StationDto.java               # Data transfer object
```

### Key Design Patterns

1. **Layered Architecture**
   - Controller layer for HTTP handling
   - Service layer for business logic
   - Client layer for external API integration

2. **Dependency Injection**
   - Constructor injection with `@RequiredArgsConstructor`
   - Configuration-based bean creation

3. **Reactive Programming**
   - WebClient for non-blocking HTTP calls
   - Reactive streams for data processing

## Configuration Management

### Application Properties

**Main Configuration** (`application.yml`):
```yaml
server:
  port: 8080

smhi:
  baseUrl: https://opendata-download-metobs.smhi.se/api/version/1.0
  cacheSeconds: 60
  stationCacheHours: 24
  defaultStations: ["159880"]

security:
  apiKey: fhn1CaoXEI6gfOVOuHbJbAbFiM8MY4PH

spring:
  cache:
    type: caffeine
```

**Environment-specific Overrides**:
- `application-dev.yml` for development
- `application-prod.yml` for production
- `application-test.yml` for testing

### Configuration Best Practices

1. **Externalize Configuration**
   - Use environment variables for sensitive data
   - Keep default values in application.yml

2. **Profile-based Configuration**
   - Use Spring profiles for different environments
   - Override sensitive settings per environment

3. **Validation**
   - Add validation annotations to configuration classes
   - Fail fast on invalid configuration

## Testing Strategy

### Test Types

1. **Unit Tests**
   - Test individual components in isolation
   - Mock external dependencies
   - Fast execution, high coverage

2. **Integration Tests**
   - Test component interactions
   - Use test containers or mocks
   - Validate end-to-end flows

3. **Web Layer Tests**
   - Test HTTP request/response handling
   - Use MockMvc for controller testing
   - Validate JSON serialization

### Test Configuration

**Test Properties** (`application-test.yml`):
```yaml
security:
  apiKey: test-key
```

**Test Configuration Class** (`TestConfig.java`):
```java
@TestConfiguration
public class TestConfig {
  @Bean
  @Primary
  ObservationService observationServiceStub(SmhiClient smhi) {
    // Test-specific service implementation
  }
}
```

### Running Tests

```bash
# All tests
mvn test

# Specific test class
mvn test -Dtest=ObservationServiceMergeTest

# Integration tests with WireMock
mvn test -Dtest=*IntegrationTest

# Test with coverage
mvn test jacoco:report
```

## API Development

### Adding New Endpoints

1. **Controller Method**
   ```java
   @GetMapping("/new-endpoint")
   @Operation(summary = "Description of endpoint")
   public ResponseEntity<ResponseType> newEndpoint(
       @RequestParam String param) {
     // Implementation
   }
   ```

2. **Service Method**
   ```java
   public ResponseType processRequest(String param) {
     // Business logic
   }
   ```

3. **Add Tests**
   ```java
   @Test
   void testNewEndpoint() {
     // Test implementation
   }
   ```

### Request/Response Validation

```java
// Request validation
@Valid @RequestBody RequestDto request

// Response validation
@Valid @ResponseBody ResponseDto response
```

### Error Handling

```java
@ExceptionHandler(ValidationException.class)
public ResponseEntity<ErrorResponse> handleValidation(
    ValidationException ex) {
  return ResponseEntity.badRequest()
      .body(new ErrorResponse(ex.getMessage()));
}
```

## Data Processing

### SMHI API Integration

**Parameter Mapping**:
- Parameter 1: Air Temperature (Lufttemperatur)
- Parameter 4: Wind Speed (Vindhastighet)
- Parameter 21: Wind Gust (Byvind)

**Data Merging Logic**:
```java
// 1. Fetch data for each parameter
Map<Long, Double> gust = toMap(gustSeries);
Map<Long, Double> temp = toMap(tempSeries);
Map<Long, Double> wind = toMap(windSeries);

// 2. Create union of timestamps
Set<Long> allTimestamps = new TreeSet<>();
allTimestamps.addAll(gust.keySet());
allTimestamps.addAll(temp.keySet());
allTimestamps.addAll(wind.keySet());

// 3. Merge into observation points
for (Long ts : allTimestamps) {
  Instant timestamp = Instant.ofEpochMilli(ts);
  mergedPoints.add(new ObservationPoint(
    stationId, stationName, lat, lon,
    timestamp,
    gust.get(ts),    // Can be null
    temp.get(ts),    // Can be null
    wind.get(ts)     // Can be null
  ));
}
```

### Geographic Filtering

**Haversine Distance Calculation**:
```java
private static double haversineKm(double lat1, double lon1, 
                                 double lat2, double lon2) {
  final double R = 6371.0; // Earth's radius in km
  double dLat = Math.toRadians(lat2 - lat1);
  double dLon = Math.toRadians(lon2 - lon1);
  double a = Math.sin(dLat/2) * Math.sin(dLat/2)
      + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
      * Math.sin(dLon/2) * Math.sin(dLon/2);
  double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
  return R * c;
}
```

## Caching Strategy

### Cache Configuration

```java
@Configuration
@EnableCaching
public class CacheConfig {
  
  @Bean
  public CacheManager cacheManager() {
    CaffeineCacheManager cacheManager = new CaffeineCacheManager();
    cacheManager.setCaffeine(Caffeine.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(Duration.ofMinutes(5)));
    return cacheManager;
  }
}
```

### Cache Usage

```java
@Cacheable("stations")
public List<StationDto> getStations(String set) {
  // Expensive operation
}

@CacheEvict("stations")
public void clearStationCache() {
  // Clear cache
}
```

## Security Implementation

### API Key Filter

```java
@Component
@Order(1)
public class ApiKeyFilter implements Filter {
  
  @Value("${security.apiKey}")
  String configuredKey;
  
  @Override
  public void doFilter(ServletRequest req, ServletResponse res, 
                      FilterChain chain) {
    // Validation logic
  }
}
```

### Security Best Practices

1. **Environment Variables**
   ```bash
   export SMHI_API_KEY="your-secure-key"
   ```

2. **Configuration Override**
   ```yaml
   security:
     apiKey: ${SMHI_API_KEY:default-key}
   ```

3. **Request Validation**
   - Validate API key format
   - Log authentication attempts
   - Rate limiting for failed attempts

## Performance Optimization

### HTTP Client Configuration

```java
@Bean
WebClient smhiWebClient(@Value("${smhi.baseUrl}") String baseUrl) {
  HttpClient http = HttpClient.create()
      .compress(true)
      .responseTimeout(Duration.ofSeconds(15))
      .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);
  
  return WebClient.builder()
      .baseUrl(baseUrl)
      .clientConnector(new ReactorClientHttpConnector(http))
      .codecs(c -> c.defaultCodecs().maxInMemorySize(8 * 1024 * 1024))
      .build();
}
```

### Memory Management

1. **Stream Processing**
   ```java
   return observations.stream()
       .filter(predicate)
       .sorted(comparator)
       .collect(Collectors.toList());
   ```

2. **Lazy Loading**
   ```java
   public Stream<ObservationPoint> getObservationsStream() {
     return fetchData().stream();
   }
   ```

## Monitoring and Logging

### Application Metrics

```java
@Component
public class MetricsCollector {
  
  private final MeterRegistry meterRegistry;
  
  public void recordApiCall(String endpoint, int statusCode) {
    Timer.Sample sample = Timer.start(meterRegistry);
    sample.stop(Timer.builder("api.calls")
        .tag("endpoint", endpoint)
        .tag("status", String.valueOf(statusCode))
        .register(meterRegistry));
  }
}
```

### Logging Configuration

```yaml
logging:
  level:
    backend: DEBUG
    org.springframework.web: INFO
    reactor.netty: WARN
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

## Deployment

### Docker Configuration

```dockerfile
FROM openjdk:21-jre-slim

COPY target/backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Environment Variables

```bash
# Production environment
export SMHI_API_KEY="production-key"
export SMHI_BASE_URL="https://opendata-download-metobs.smhi.se/api/version/1.0"
export SERVER_PORT="8080"
```

### Health Checks

```java
@Component
public class SmhiHealthIndicator implements HealthIndicator {
  
  @Override
  public Health health() {
    // Check SMHI API availability
    return Health.up()
        .withDetail("smhi-api", "available")
        .build();
  }
}
```

## Troubleshooting

### Common Issues

1. **SMHI API Timeout**
   - Check network connectivity
   - Verify SMHI API status
   - Increase timeout values

2. **Memory Issues**
   - Monitor heap usage
   - Adjust JVM parameters
   - Optimize data processing

3. **Authentication Failures**
   - Verify API key configuration
   - Check request headers
   - Validate filter configuration

### Debug Configuration

```yaml
logging:
  level:
    backend: DEBUG
    org.springframework.web.client: DEBUG
    reactor.netty.http.client: DEBUG
```

### Performance Profiling

```bash
# Enable JVM profiling
java -XX:+UnlockDiagnosticVMOptions \
     -XX:+DebugNonSafepoints \
     -XX:+PreserveFramePointer \
     -jar target/backend-0.0.1-SNAPSHOT.jar
```
