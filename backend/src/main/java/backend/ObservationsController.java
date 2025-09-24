package backend;

import java.time.Instant;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@Tag(name = "Observations")
@RequiredArgsConstructor
public class ObservationsController {
  private final ObservationService svc;

  @GetMapping("/stations")
  @Operation(
      summary = "Get weather stations",
      description = "Retrieves a list of available weather stations with optional filtering by station set type."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved stations",
          content = @Content(mediaType = "application/json",
              examples = @ExampleObject(value = """
                  [
                    {
                      "id": "159880",
                      "name": "Stockholm A",
                      "lat": 59.3,
                      "lon": 18.1
                    }
                  ]
                  """))),
      @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid API key")
  })
  public List<StationDto> stations(
      @Parameter(description = "Station set type: 'core' (major stations), 'additional' (other stations), or 'all' (all stations)", 
                 example = "core")
      @RequestParam(defaultValue = "core") String set) {
    return svc.getStations(set);
  }

  @GetMapping("/observations")
  @Operation(
      summary = "Get merged weather observations",
      description = "Retrieves merged weather observations combining temperature (Lufttemperatur), wind gusts (Byvind), and wind speed (Vindhastighet) data from SMHI weather stations."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved observations",
          content = @Content(mediaType = "application/json",
              examples = @ExampleObject(value = """
                  [
                    {
                      "stationId": "159880",
                      "stationName": "Stockholm A",
                      "lat": 59.3,
                      "lon": 18.1,
                      "timestampUtc": "2025-01-23T10:00:00Z",
                      "gustMs": 12.4,
                      "airTempC": 17.1,
                      "windSpeedMs": 5.6
                    }
                  ]
                  """))),
      @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid API key")
  })
  public List<ObservationPoint> list(
      @Parameter(description = "Comma-separated list of station IDs", example = "159880,159881")
      @RequestParam(required = false) String stationId,
      
      @Parameter(description = "Time range for observations", example = "last-hour")
      @RequestParam(required = false, defaultValue = "last-hour") String range,
      
      @Parameter(description = "Start timestamp (ISO 8601 format)", example = "2025-01-23T10:00:00Z")
      @RequestParam(required = false) Instant from,
      
      @Parameter(description = "End timestamp (ISO 8601 format)", example = "2025-01-23T12:00:00Z")
      @RequestParam(required = false) Instant to,
      
      @Parameter(description = "Latitude for geographic filtering", example = "59.3")
      @RequestParam(required = false) Double lat,
      
      @Parameter(description = "Longitude for geographic filtering", example = "18.1")
      @RequestParam(required = false) Double lon,
      
      @Parameter(description = "Search radius in kilometers", example = "50")
      @RequestParam(required = false) Double radiusKm) {
    return svc.getMergedObservations(stationId, range, from, to, lat, lon, radiusKm);
  }
}


