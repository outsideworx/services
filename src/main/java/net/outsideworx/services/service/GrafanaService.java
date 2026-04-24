package net.outsideworx.services.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public final class GrafanaService {
    private final MeterRegistry registry;

    public void registerException(String type) {
        getCounter("services_exceptions", "type", type)
                .increment();
    }

    public void registerRequest(String endpoint, String fetch) {
        getCounter("services_requests", "endpoint", endpoint, "fetch", fetch)
                .increment();
    }

    private Counter getCounter(String name, String... tags) {
        return Counter.builder(name)
                .tags(tags)
                .register(registry);
    }
}