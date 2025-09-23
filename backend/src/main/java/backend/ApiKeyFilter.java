package backend;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class ApiKeyFilter implements Filter {
  @Value("${security.apiKey}") String configured;

  @Override public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
      throws IOException, ServletException {
    var http = (HttpServletRequest) req;
    var out  = (HttpServletResponse) res;
    var key = http.getHeader("x-api-key");
    if (configured == null || !configured.equals(key)) {
      out.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      out.setContentType("application/json");
      out.getWriter().write("{\"error\":\"invalid api key\"}");
      return;
    }
    chain.doFilter(req, res);
  }
}


