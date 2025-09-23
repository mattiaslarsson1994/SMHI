package backend;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {SmhiBackendApplication.class, TestConfig.class})
@ActiveProfiles("test")
@DisplayName("Integration: HTTP -> controller -> service stub")
class ObservationsIntegrationTest {

  static WireMockServer wm;

  @LocalServerPort int port;

  @BeforeAll
  static void startWireMock() {
    wm = new WireMockServer(0); // random port
    wm.start();
  }

  @DynamicPropertySource
  static void overrideProps(DynamicPropertyRegistry registry) {
    registry.add("smhi.baseUrl", () -> wm.baseUrl() + "/api/version/1.0");
  }

  @AfterAll
  static void stopWireMock() {
    wm.stop();
  }

  @BeforeEach
  void stubSmhi() {
    // sample minimal JSON for gust/temp/wind
    String dataJson = """
      { "value": [
        {"date": 1758621600000, "value": 12.4}
      ]}
      """;
    // stub parameter 21/1/4 for a fake station 159880, last-hour
    wm.stubFor(get(urlMatching("/api/version/1.0/parameter/21/station/159880/period/latest-hour/data.json"))
        .willReturn(okJson(dataJson)));
    wm.stubFor(get(urlMatching("/api/version/1.0/parameter/1/station/159880/period/latest-hour/data.json"))
        .willReturn(okJson(dataJson)));
    wm.stubFor(get(urlMatching("/api/version/1.0/parameter/4/station/159880/period/latest-hour/data.json"))
        .willReturn(okJson(dataJson)));
  }

  @Test
  @DisplayName("Returns >0 merged observations with test key and last-hour")
  void endToEnd_returnsMergedObservation() {
    // call our API with test key
    var client = WebClient.builder()
        .baseUrl("http://localhost:" + port)
        .defaultHeader("x-api-key", "test-key")
        .build();

    List<?> rows = client.get()
        .uri("/api/observations?stationId=159880&range=last-hour")
        .retrieve()
        .bodyToMono(List.class)
        .block();

    System.out.println("GET /api/observations?stationId=159880&range=last-hour -> rows=" + (rows == null ? "null" : rows.size()));
    assertThat(rows).isNotNull();
    assertThat(rows.size()).isGreaterThan(0);
  }

  @Test
  @DisplayName("Returns >0 rows for last-day with stationId")
  void lastDay_withStationId_returnsRows() {
    // stub last-day
    String dataJson = """
      { "value": [
        {"date": 1758621600000, "value": 12.4},
        {"date": 1758625200000, "value": 8.3}
      ]}
      """;
    wm.stubFor(get(urlMatching("/api/version/1.0/parameter/21/station/159880/period/latest-day/data.json"))
        .willReturn(okJson(dataJson)));
    wm.stubFor(get(urlMatching("/api/version/1.0/parameter/1/station/159880/period/latest-day/data.json"))
        .willReturn(okJson(dataJson)));

    var client = WebClient.builder()
        .baseUrl("http://localhost:" + port)
        .defaultHeader("x-api-key", "test-key")
        .build();

    List<?> rows = client.get()
        .uri("/api/observations?stationId=159880&range=last-day")
        .retrieve()
        .bodyToMono(List.class)
        .block();

    System.out.println("GET /api/observations?stationId=159880&range=last-day -> rows=" + (rows == null ? "null" : rows.size()));
    assertThat(rows).isNotNull();
    assertThat(rows.size()).isGreaterThan(0);
  }

  @Test
  @DisplayName("Defaults to latest-hour for defaultStations when no params")
  void default_noParams_latestHour() {
    // reuse latest-hour stubs from stubSmhi (ensure present)
    String dataJson = """
      { "value": [
        {"date": 1758621600000, "value": 12.4}
      ]}
      """;
    wm.stubFor(get(urlMatching("/api/version/1.0/parameter/21/station/159880/period/latest-hour/data.json"))
        .willReturn(okJson(dataJson)));
    wm.stubFor(get(urlMatching("/api/version/1.0/parameter/1/station/159880/period/latest-hour/data.json"))
        .willReturn(okJson(dataJson)));

    var client = WebClient.builder()
        .baseUrl("http://localhost:" + port)
        .defaultHeader("x-api-key", "test-key")
        .build();

    List<?> rows = client.get()
        .uri("/api/observations")
        .retrieve()
        .bodyToMono(List.class)
        .block();

    System.out.println("GET /api/observations (no params) -> rows=" + (rows == null ? "null" : rows.size()));
    assertThat(rows).isNotNull();
    assertThat(rows.size()).isGreaterThan(0);
  }
}
