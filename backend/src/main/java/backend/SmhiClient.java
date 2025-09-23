package backend;

import java.time.Instant;
import java.util.List;
import java.util.Map;

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


