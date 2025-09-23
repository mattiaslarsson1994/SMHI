package backend;

import java.io.IOException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class ApiKeyFilter implements Filter {

  @Value("${security.apiKey}")
  String configured;

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest http = (HttpServletRequest) req;
    HttpServletResponse out  = (HttpServletResponse) res;

    String path = http.getRequestURI();
    String method = http.getMethod();

    // allow Swagger UI + assets + OpenAPI JSON + favicon + health + CORS preflight
    boolean swagger = path.equals("/swagger-ui.html")
        || path.startsWith("/swagger-ui/")
        || path.startsWith("/v3/api-docs/");
    boolean publicPaths = swagger
        || path.equals("/favicon.ico")
        || path.equals("/actuator/health");
    boolean preflight = "OPTIONS".equalsIgnoreCase(method);

    if (publicPaths || preflight) {
      chain.doFilter(req, res);
      return;
    }

    String key = http.getHeader("x-api-key");
    if (configured == null || !configured.equals(key)) {
      out.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      out.setContentType("application/json");
      out.getWriter().write("{\"error\":\"invalid api key\"}");
      return;
    }

    chain.doFilter(req, res);
  }
}