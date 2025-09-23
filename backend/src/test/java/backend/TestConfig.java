package backend;

import java.time.Instant;
import java.util.List;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestConfig {
  @Bean
  @Primary
  ObservationService observationServiceStub() {
    return new ObservationService() {
      @Override
      public List<ObservationPoint> getMergedObservations(
          String stationId,
          String range,
          Instant from,
          Instant to,
          Double lat,
          Double lon,
          Double radiusKm) {
        return List.of(new ObservationPoint(
            "159880", "Stockholm A", 59.3, 18.1,
            Instant.parse("2025-09-23T10:00:00Z"), 12.4, 17.1, 5.6));
      }
    };
  }
}


