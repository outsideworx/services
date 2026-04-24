package net.outsideworx.services.gateway;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GrafanaGatewayTest {
    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();

    private final GrafanaGateway grafanaGateway = new GrafanaGateway(registry);

    @Test
    void registerException_incrementsExceptionCounter() {
        grafanaGateway.registerException("bad_credentials");

        Counter counter = registry.find("services_exceptions").tag("type", "bad_credentials").counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    void registerException_incrementsCounterOnEachCall() {
        grafanaGateway.registerException("bad_credentials");
        grafanaGateway.registerException("bad_credentials");

        assertThat(registry.find("services_exceptions").tag("type", "bad_credentials").counter().count()).isEqualTo(2.0);
    }

    @Test
    void registerRequest_incrementsRequestCounter() {
        grafanaGateway.registerRequest("gaiapeeps", "all");

        Counter counter = registry.find("services_requests").tag("endpoint", "gaiapeeps").tag("fetch", "all").counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    void registerRequest_differentTagsProduceSeparateCounters() {
        grafanaGateway.registerRequest("gaiapeeps", "all");
        grafanaGateway.registerRequest("soupart", "art");

        assertThat(registry.find("services_requests").tag("endpoint", "gaiapeeps").counter().count()).isEqualTo(1.0);
        assertThat(registry.find("services_requests").tag("endpoint", "soupart").counter().count()).isEqualTo(1.0);
    }
}