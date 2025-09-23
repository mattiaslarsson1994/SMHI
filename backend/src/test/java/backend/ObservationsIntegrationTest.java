package backend;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
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
}
