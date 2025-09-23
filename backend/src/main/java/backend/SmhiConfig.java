package backend;

import io.netty.channel.ChannelOption;
import reactor.netty.http.client.HttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Configuration
public class SmhiConfig {
  @Bean
  WebClient smhiWebClient(@Value("${smhi.baseUrl}") String baseUrl, WebClient.Builder builder) {
    HttpClient http = HttpClient.create()
        .compress(true) // gzip if server supports it
        .responseTimeout(Duration.ofSeconds(15))
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);

    return builder
        .baseUrl(baseUrl)
        .clientConnector(new ReactorClientHttpConnector(http))
        .codecs(c -> c.defaultCodecs().maxInMemorySize(8 * 1024 * 1024)) // 8 MB
        .build();
  }
}


