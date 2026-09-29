package net.outsideworx.services.configuration.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public final class FilterConditions {
    private static final Function<HttpServletRequest, String> GET_CALLER = request -> request.getHeader("X-Caller-Id");
    private static final Function<HttpServletRequest, String> GET_TOKEN = request -> request.getHeader("X-Auth-Token");
    private final Properties properties;

    public boolean apiRequest(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/api");
    }

    public boolean cachedApiRequest(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/api/cache");
    }

    public boolean invalidCallerIdOrAuthToken(HttpServletRequest request) {
        return properties.getClients().values()
                .stream()
                .noneMatch(client -> isValidClient(request, client) || isOutsideworx(request));
    }

    public boolean notPreflightRequest(HttpServletRequest request) {
        return !"OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    private boolean isValidClient(HttpServletRequest request, Properties.Client client) {
        return client.getCaller().equals(GET_CALLER.apply(request)) && client.getToken().equals(GET_TOKEN.apply(request));
    }

    private boolean isOutsideworx(HttpServletRequest request) {
        Properties.Client outsideworx = properties.getClients().get("outsideworx");
        if (Objects.isNull(outsideworx)) {
            throw new IllegalStateException("Credential lookup failed.");
        }
        return outsideworx.getCaller().equals(GET_CALLER.apply(request)) && outsideworx.getToken().equals(GET_TOKEN.apply(request));
    }
}