package backend;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.io.IOException;

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

    String path   = http.getRequestURI();
    String method = http.getMethod();

    boolean swaggerUi   = path.equals("/swagger-ui.html") || path.equals("/swagger-ui/index.html") || path.startsWith("/swagger-ui/");
    boolean openApiJson = path.equals("/v3/api-docs") || path.startsWith("/v3/api-docs/");
    boolean publicPaths = swaggerUi || openApiJson || path.equals("/favicon.ico") || path.equals("/actuator/health");
    boolean preflight   = "OPTIONS".equalsIgnoreCase(method);

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