package backend;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ObservationService {
  private final SmhiClient smhi;
  private final List<StationDto> defaultStations;

  public ObservationService(SmhiClient smhi,
                            @Value("${smhi.defaultStations:159880}") List<String> stationIds) {
    this.smhi = smhi;
    this.defaultStations = stationIds.stream()
        .map(id -> new StationDto(id, "Unknown", 0, 0))
        .toList();
  }

  public List<StationDto> getStations(String set) {
    return defaultStations;
  }

  public List<ObservationPoint> getMergedObservations(
      String stationId,
      String range,
      Instant from,
      Instant to,
      Double lat,
      Double lon,
      Double radiusKm) {
    String effectiveRange = range == null || range.isBlank() ? "last-hour" : range;
    List<StationDto> stations = stationId != null ? List.of(new StationDto(stationId, "Unknown", 0, 0)) : defaultStations;

    List<ObservationPoint> mergedAll = new ArrayList<>();
    for (StationDto s : stations) {
      var gustSeries = switch (effectiveRange) {
        case "last-day" -> smhi.fetchLatestDay(s.id(), 21);
        default -> smhi.fetchLatestHour(s.id(), 21);
      };
      var tempSeries = switch (effectiveRange) {
        case "last-day" -> smhi.fetchLatestDay(s.id(), 1);
        default -> smhi.fetchLatestHour(s.id(), 1);
      };

      Map<Instant, Double> gustByTs = toMap(gustSeries);
      Map<Instant, Double> tempByTs = toMap(tempSeries);
      var allTs = new TreeSet<Instant>();
      allTs.addAll(gustByTs.keySet());
      allTs.addAll(tempByTs.keySet());
      for (Instant ts : allTs) {
        mergedAll.add(new ObservationPoint(
            s.id(), s.name(), s.lat(), s.lon(),
            ts,
            gustByTs.get(ts),
            tempByTs.get(ts),
            null
        ));
      }
    }
    return mergedAll;
  }

  private static Map<Instant, Double> toMap(SmhiClient.SmhiSeries series) {
    Map<Instant, Double> map = new LinkedHashMap<>();
    if (series != null && series.value != null) {
      for (SmhiClient.ValuePoint vp : series.value) {
        Instant ts = vp.asInstantUtc();
        if (ts != null) map.put(ts, vp.value);
      }
    }
    return map;
  }
}


