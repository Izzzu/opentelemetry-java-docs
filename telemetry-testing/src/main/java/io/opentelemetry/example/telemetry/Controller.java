package io.opentelemetry.example.telemetry;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
public class Controller {

  private static final Logger LOGGER = LogManager.getLogger(Controller.class);

  private final Random random = new Random();
  private final Tracer tracer;
  private final Meter meter;
  private final LongHistogram doWorkHistogram;

  @Autowired
  Controller(OpenTelemetry openTelemetry) {
    tracer = openTelemetry.getTracer(Application.class.getName());
    meter = openTelemetry.getMeter(Application.class.getName());
    doWorkHistogram = meter.histogramBuilder("do-work").ofLongs().build();
  }

  @GetMapping("/ping")
  public String ping() throws InterruptedException {
    int sleepTime = random.nextInt(200);
    System.out.println("record");
    doWorkHistogram.record(sleepTime, Attributes.of(AttributeKey.stringKey("method"), "ping"));
    doWork(sleepTime);
    return "pong";
  }

  @WithSpan
  private void doWork(int sleepTime) throws InterruptedException {
      Thread.sleep(sleepTime);
      LOGGER.info("A sample log message!");
  }
}
