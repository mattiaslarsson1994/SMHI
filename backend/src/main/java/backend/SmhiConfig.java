package backend;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class SmhiConfig {
  @Bean WebClient smhiWebClient(@Value("${smhi.baseUrl}") String base) {
    return WebClient.builder().baseUrl(base).build();
  }
}


