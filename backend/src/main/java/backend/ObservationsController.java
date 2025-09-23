package backend;

import java.time.Instant;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@Tag(name = "Observations")
@RequiredArgsConstructor
public class ObservationsController {
  private final ObservationService svc;

  @GetMapping("/stations")
  public List<StationDto> stations(@RequestParam(defaultValue = "core") String set) {
    return svc.getStations(set); // "core" | "additional" | "all"
  }

  @GetMapping("/observations")
  @Operation(summary = "Merged Byvind + Lufttemperatur (+Vindhastighet)")
  public List<ObservationPoint> list(
      @RequestParam(required = false) String stationId,
      @RequestParam(required = false, defaultValue = "last-hour") String range,
      @RequestParam(required = false) Instant from,
      @RequestParam(required = false) Instant to,
      @RequestParam(required = false) Double lat,
      @RequestParam(required = false) Double lon,
      @RequestParam(required = false) Double radiusKm) {
    return svc.getMergedObservations(stationId, range, from, to, lat, lon, radiusKm);
  }
}


