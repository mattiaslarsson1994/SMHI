package backend;

import java.time.Instant;

// unified output row
public record ObservationPoint(
  String stationId, String stationName, double lat, double lon,
  Instant timestampUtc,
  Double gustMs,      // param 21
  Double airTempC,    // param 1
  Double windSpeedMs  // param 4
) {}


