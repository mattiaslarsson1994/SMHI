package backend;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class ObservationService {
  public List<StationDto> getStations(String set) {
    return Collections.emptyList();
  }

  public List<ObservationPoint> getMergedObservations(
      String stationId,
      String range,
      Instant from,
      Instant to,
      Double lat,
      Double lon,
      Double radiusKm) {
    return Collections.emptyList();
  }
}


