package application.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GrafanaServiceTest {
    private SimpleMeterRegistry registry;
    private GrafanaService grafanaService;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        grafanaService = new GrafanaService(registry);
    }

    @Test
    void registerException_incrementsExceptionCounter() {
        grafanaService.registerException("bad_credentials");

        Counter counter = registry.find("services_exceptions").tag("type", "bad_credentials").counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    void registerException_incrementsCounterOnEachCall() {
        grafanaService.registerException("bad_credentials");
        grafanaService.registerException("bad_credentials");

        assertThat(registry.find("services_exceptions").tag("type", "bad_credentials").counter().count()).isEqualTo(2.0);
    }

    @Test
    void registerRequest_incrementsRequestCounter() {
        grafanaService.registerRequest("gaiapeeps", "all");

        Counter counter = registry.find("services_requests").tag("endpoint", "gaiapeeps").tag("fetch", "all").counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    void registerRequest_differentTagsProduceSeparateCounters() {
        grafanaService.registerRequest("gaiapeeps", "all");
        grafanaService.registerRequest("soupart", "art");

        assertThat(registry.find("services_requests").tag("endpoint", "gaiapeeps").counter().count()).isEqualTo(1.0);
        assertThat(registry.find("services_requests").tag("endpoint", "soupart").counter().count()).isEqualTo(1.0);
    }
}
