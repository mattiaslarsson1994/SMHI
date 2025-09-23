package backend;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/** Minimal client for SMHI MetObs parameter series. */
@Component
public class SmhiClient {
  private final WebClient smhiClient;

  public SmhiClient(WebClient smhiWebClient) {
    this.smhiClient = smhiWebClient;
  }

  /** Fetch latest-hour values for a station and parameter id (e.g., 21 or 1). */
  public SmhiSeries fetchLatestHour(String stationId, int parameterId) {
    String path = "/parameter/" + parameterId + "/station/" + stationId + "/period/latest-hour/data.json";
    return smhiClient.get()
        .uri(path)
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .bodyToMono(SmhiSeries.class)
        .block();
  }

  /** Fetch latest-day values for a station and parameter id (e.g., 21 or 1). */
  public SmhiSeries fetchLatestDay(String stationId, int parameterId) {
    String path = "/parameter/" + parameterId + "/station/" + stationId + "/period/latest-day/data.json";
    return smhiClient.get()
        .uri(path)
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .bodyToMono(SmhiSeries.class)
        .block();
  }

  public List<StationDto> fetchStationsFromParameter(int parameterId) {
    JsonNode root = smhiClient.get()
        .uri("/parameter/" + parameterId + ".json")
        .retrieve()
        .bodyToMono(JsonNode.class)
        .block();
  
    if (root == null) return List.of();
  
    // Prefer common SMHI naming: "station" (singular), but also try "stations"
    JsonNode arr = root.get("station");
    if (arr == null || !arr.isArray()) {
      arr = root.get("stations");
    }
  
    List<StationDto> out = new java.util.ArrayList<>();
    if (arr != null && arr.isArray()) {
      for (JsonNode s : arr) {
        StationDto dto = toStationDtoOrNull(s);
        if (dto != null) out.add(dto);
      }
    } else {
      // Defensive: scan any array field that looks like a station array
      var fields = root.fields();
      while (fields.hasNext()) {
        var e = fields.next();
        JsonNode v = e.getValue();
        if (v != null && v.isArray()) {
          for (JsonNode s : v) {
            StationDto dto = toStationDtoOrNull(s);
            if (dto != null) out.add(dto);
          }
        }
      }
    }
    return out;
  }
  
  private static StationDto toStationDtoOrNull(JsonNode s) {
    if (s == null || !s.isObject()) return null;
  
    // id can be number or text
    String id = s.has("id") ? (s.get("id").isNumber() ? s.get("id").asText() : s.get("id").asText(null)) : null;
  
    // Some dumps use "name" or "name_en" — prefer "name", fallback to whatever exists
    String name = s.hasNonNull("name") ? s.get("name").asText()
        : s.hasNonNull("name_en") ? s.get("name_en").asText()
        : "Unknown";
  
    // lat/lon keys are typically "latitude"/"longitude"
    double lat = s.has("latitude") ? s.get("latitude").asDouble(0) : 0;
    double lon = s.has("longitude") ? s.get("longitude").asDouble(0) : 0;
  
    if (id == null) return null;
    return new StationDto(id, name, lat, lon);
  }  

  /** POJO for SMHI series subset: we only need value list with date/value. */
  public static class SmhiSeries {
    public List<ValuePoint> value;
  }

  public static class ValuePoint {
    public Long date;     // epoch millis
    public Double value;  // measurement

    public Instant asInstantUtc() { return date == null ? null : Instant.ofEpochMilli(date); }
  }
}


