package backend;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ObservationService {

  private static final int PARAM_TEMP = 1;   // Lufttemperatur
  private static final int PARAM_WIND = 4;   // Vindhastighet
  private static final int PARAM_GUST = 21;  // Byvind

  private final SmhiClient smhi;

  public ObservationService(SmhiClient smhi,
                            @Value("${smhi.defaultStations:159880}") List<String> stationIds) {
    this.smhi = smhi;
    // Note: stationIds parameter kept for potential future use
  }

  public List<ObservationPoint> getMergedObservations(
      String stationIdCsv,
      String range,                 // "last-hour" | "last-day" (controller should validate)
      Instant from,
      Instant to,
      Double lat,
      Double lon,
      Double radiusKm
  ) {
    final String effectiveRange = (range == null || range.isBlank()) ? "last-hour" : range;

    // 1) Resolve stations
    final List<StationDto> stations = resolveStations(stationIdCsv);

    // 2) Fetch series per station and merge
    final List<ObservationPoint> mergedAll = new ArrayList<>(1024);

    for (StationDto s : stations) {
      SmhiClient.SmhiSeries gustSeries;
      SmhiClient.SmhiSeries tempSeries;
      SmhiClient.SmhiSeries windSeries;

      if ("last-day".equals(effectiveRange)) {
        gustSeries = smhi.fetchLatestDay(s.id(), PARAM_GUST);
        tempSeries = smhi.fetchLatestDay(s.id(), PARAM_TEMP);
        windSeries = smhi.fetchLatestDay(s.id(), PARAM_WIND);
      } else { // last-hour
        gustSeries = smhi.fetchLatestHour(s.id(), PARAM_GUST);
        tempSeries = smhi.fetchLatestHour(s.id(), PARAM_TEMP);
        windSeries = smhi.fetchLatestHour(s.id(), PARAM_WIND);
      }

      Map<Long, Double> gust = toMap(gustSeries);
      Map<Long, Double> temp = toMap(tempSeries);
      Map<Long, Double> wind = toMap(windSeries);

      // union of timestamps present in any series
      var allTs = new TreeSet<Long>();
      allTs.addAll(gust.keySet());
      allTs.addAll(temp.keySet());
      allTs.addAll(wind.keySet());

      for (Long ts : allTs) {
        Instant t = Instant.ofEpochMilli(ts);
        mergedAll.add(new ObservationPoint(
            s.id(), s.name(), s.lat(), s.lon(),
            t,
            gust.get(ts),       // gustMs (can be null)
            temp.get(ts),       // airTempC (can be null)
            wind.get(ts)        // windSpeedMs (can be null)
        ));
      }
    }

    // 3) Filters: time window & geo
    Stream<ObservationPoint> stream = mergedAll.stream();

    if (from != null) stream = stream.filter(p -> !p.timestampUtc().isBefore(from));
    if (to   != null) stream = stream.filter(p -> !p.timestampUtc().isAfter(to));

    if (lat != null && lon != null && radiusKm != null && radiusKm > 0) {
      final double oLat = lat, oLon = lon, r = radiusKm;
      stream = stream.filter(p -> haversineKm(oLat, oLon, p.lat(), p.lon()) <= r);
    }

    return stream
        .sorted(Comparator
            .comparing(ObservationPoint::timestampUtc).reversed()
            .thenComparing(ObservationPoint::stationId))
        .toList();
  }

  /** Stations endpoint helper. Filters stations by set type: core, additional, or all. */
  public List<StationDto> getStations(String set) {
    List<StationDto> allStations = smhi.fetchStationsFromParameter(PARAM_TEMP);
    
    if (set == null || set.isBlank() || "all".equalsIgnoreCase(set)) {
      return allStations;
    }
    
    switch (set.toLowerCase()) {
      case "core":
        return allStations.stream()
            .filter(s -> isCoreStation(s.id()))
            .toList();
      case "additional":
        return allStations.stream()
            .filter(s -> !isCoreStation(s.id()))
            .toList();
      default:
        return allStations;
    }
  }

  // ---------- helpers ----------

  private List<StationDto> resolveStations(String stationIdCsv) {
    if (stationIdCsv != null && !stationIdCsv.isBlank()) {
      Set<String> ids = Arrays.stream(stationIdCsv.split(","))
          .map(String::trim).filter(s -> !s.isBlank())
          .collect(Collectors.toCollection(LinkedHashSet::new));

      // try to enrich with real metadata
      Map<String, StationDto> byId = smhi.fetchStationsFromParameter(PARAM_TEMP)
          .stream().collect(Collectors.toMap(StationDto::id, s -> s, (a, b) -> a));

      List<StationDto> out = new ArrayList<>(ids.size());
      for (String id : ids) {
        StationDto meta = byId.get(id);
        if (meta != null) out.add(meta);
        else out.add(new StationDto(id, "Unknown", 0.0, 0.0));
      }
      return out;
    }

    // Default: ALL stations (per PDF)
    return smhi.fetchStationsFromParameter(PARAM_TEMP);
  }

  /** Convert your SmhiSeries (value list) to a map keyed by epochMillis. */
  private static Map<Long, Double> toMap(SmhiClient.SmhiSeries series) {
    if (series == null || series.value == null) return Collections.emptyMap();
    Map<Long, Double> m = new HashMap<>(series.value.size() * 2);
    for (SmhiClient.ValuePoint vp : series.value) {
      if (vp != null && vp.date != null) {
        m.put(vp.date, vp.value);
      }
    }
    return m;
  }

  /** Haversine distance in kilometers */
  private static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
    final double R = 6371.0;
    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);
    double a = Math.sin(dLat/2)*Math.sin(dLat/2)
        + Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2))
        * Math.sin(dLon/2)*Math.sin(dLon/2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    return R * c;
  }

  /** Determines if a station is a core station based on its ID. */
  private static boolean isCoreStation(String stationId) {
    // Core stations are typically major weather stations in Sweden
    // This is a simplified implementation - in practice, you might want to
    // maintain a list of core station IDs or determine this based on station metadata
    Set<String> coreStationIds = Set.of(
        "159880", // Stockholm A
        "159881", // Göteborg A  
        "159882", // Malmö A
        "159883", // Uppsala A
        "159884", // Linköping A
        "159885", // Örebro A
        "159886", // Västerås A
        "159887", // Norrköping A
        "159888", // Helsingborg A
        "159889"  // Jönköping A
    );
    return coreStationIds.contains(stationId);
  }
}