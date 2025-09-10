package ar.edu.utn.dds.k3003.config;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;

@Component
public class MetricsFilter extends OncePerRequestFilter {

  private final MeterRegistry meterRegistry;

  public MetricsFilter(MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;
  }

  @Override
  protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain
  ) throws ServletException, IOException {

    try {
      filterChain.doFilter(request, response);
    } finally {
      String method = request.getMethod();
      Object patternAttr = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
      String endpoint = patternAttr != null ? patternAttr.toString() : request.getRequestURI();
      int status = response.getStatus();
      String resultado = status >= 200 && status < 400 ? "success" : "error";

      meterRegistry.counter(
        "app.responses",
        "endpoint", endpoint,
        "method", method,
        "status", Integer.toString(status),
        "resultado", resultado
      ).increment();

      // If desired, we could also record a custom timer here using startNs
      // but standard http.server.requests already captures latency.
    }
  }
}