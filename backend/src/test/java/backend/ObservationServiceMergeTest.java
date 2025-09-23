package backend;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Example: test your merge logic using tiny fixture maps.
 * If your merge is inside ObservationService, extract a small static method or helper to test directly.
 */
@DisplayName("Observation merge logic")
public class ObservationServiceMergeTest {

  @Test
  @DisplayName("Merges by timestamp/station and preserves nulls")
  void mergesByTimestampAndStationAndKeepsNulls() {
    // fixture timestamps
    Instant t1 = Instant.parse("2025-09-23T10:00:00Z");
    Instant t2 = Instant.parse("2025-09-23T10:10:00Z");

    // minimal station meta
    record Station(String id, String name, double lat, double lon){}
    Station s = new Station("159880", "Stockholm A", 59.3, 18.1);

    // pretend these came from SMHI per parameter
    Map<Instant, Double> gust = Map.of(t1, 12.4, t2, 9.8);
    Map<Instant, Double> temp = Map.of(t1, 17.1);           // missing t2 on purpose
    Map<Instant, Double> wind = Map.of(t1, 5.6, t2, 4.2);

    // merge (replace with your real merge call)
    List<ObservationPoint> merged = new ArrayList<>();
    for (Instant ts : new TreeSet<>(Set.of(t1, t2))) {
      merged.add(new ObservationPoint(
          s.id(), s.name(), s.lat(), s.lon(),
          ts,
          gust.get(ts),         // gustMs
          temp.get(ts),         // airTempC (null for t2)
          wind.get(ts)          // windSpeedMs
      ));
    }

    System.out.println("Merged observations (expected 2): " + merged.size());
    merged.forEach(row -> System.out.println("  " + row));

    assertThat(merged).hasSize(2);
    var row1 = merged.get(0);
    var row2 = merged.get(1);

    assertThat(row1.timestampUtc()).isEqualTo(t1);
    assertThat(row1.gustMs()).isEqualTo(12.4);
    assertThat(row1.airTempC()).isEqualTo(17.1);
    assertThat(row1.windSpeedMs()).isEqualTo(5.6);

    assertThat(row2.timestampUtc()).isEqualTo(t2);
    assertThat(row2.gustMs()).isEqualTo(9.8);
    assertThat(row2.airTempC()).isNull();      // missing is OK (null)
    assertThat(row2.windSpeedMs()).isEqualTo(4.2);
  }
}