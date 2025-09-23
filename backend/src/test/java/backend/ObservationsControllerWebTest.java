package backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest // slices controller + filter
@ActiveProfiles("test")
class ObservationsControllerWebTest {

  @Autowired MockMvc mvc;

  @MockBean ObservationService service;

  @Test
  void blocksWithoutApiKey() throws Exception {
    mvc.perform(get("/api/observations"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void okWithApiKeyAndDefaultRange() throws Exception {
    // mock service result
    var row = new ObservationPoint("159880", "Stockholm A", 59.3, 18.1,
        Instant.parse("2025-09-23T10:00:00Z"), 12.4, 17.1, 5.6);
    given(service.getMergedObservations(any(), any(), any(), any(), any(), any(), any()))
        .willReturn(List.of(row));

    mvc.perform(get("/api/observations")
            .header("x-api-key", "test-key"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].stationId").value("159880"))
        .andExpect(jsonPath("$[0].gustMs").value(12.4));
  }

  @Test
  void respectsRangeLastDay() throws Exception {
    given(service.getMergedObservations(any(), eq("last-day"), any(), any(), any(), any(), any()))
        .willReturn(List.of());
    mvc.perform(get("/api/observations")
            .param("range", "last-day")
            .header("x-api-key", "test-key"))
        .andExpect(status().isOk());
  }
}
