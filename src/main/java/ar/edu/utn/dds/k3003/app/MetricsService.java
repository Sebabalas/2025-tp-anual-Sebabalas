package ar.edu.utn.dds.k3003.app;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

@Service
public class MetricsService {

  private final MeterRegistry meterRegistry;

  public MetricsService(MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;
  }

  public void markSuccess(String endpointPath, String httpMethod) {
    Counter counter = meterRegistry.counter(
      "app.responses",
      "endpoint", endpointPath,
      "method", httpMethod,
      "resultado", "success"
    );
    counter.increment();
  }

  public void markError(String endpointPath, String httpMethod) {
    Counter counter = meterRegistry.counter(
      "app.responses",
      "endpoint", endpointPath,
      "method", httpMethod,
      "resultado", "error"
    );
    counter.increment();
  }
}
