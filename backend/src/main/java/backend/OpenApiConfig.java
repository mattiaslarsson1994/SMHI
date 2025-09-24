package backend;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SMHI Weather Observations API")
                        .description("""
                                A REST API that fetches and merges weather observation data from SMHI 
                                (Swedish Meteorological and Hydrological Institute). The API provides 
                                endpoints to retrieve meteorological data including temperature, wind speed, 
                                and wind gusts from weather stations across Sweden.
                                
                                ## Features
                                - **Data Merging**: Combines temperature, wind gusts, and wind speed data
                                - **Geographic Filtering**: Filter observations by location and radius
                                - **Time-based Queries**: Retrieve data for the last hour or last day
                                - **Station Management**: List and query specific weather stations
                                - **API Security**: Protected endpoints with API key authentication
                                
                                ## Weather Parameters
                                - **Parameter 1**: Air Temperature (Lufttemperatur)
                                - **Parameter 4**: Wind Speed (Vindhastighet) 
                                - **Parameter 21**: Wind Gust (Byvind)
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("SMHI Weather API")
                                .email("api@smhi.se"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development server"),
                        new Server()
                                .url("https://api.smhi-weather.com")
                                .description("Production server")))
                .components(new Components()
                        .addSecuritySchemes("ApiKeyAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("x-api-key")
                                .description("API key for authentication")))
                .addSecurityItem(new SecurityRequirement().addList("ApiKeyAuth"));
    }
}
