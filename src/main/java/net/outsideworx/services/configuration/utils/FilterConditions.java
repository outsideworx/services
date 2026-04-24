package net.outsideworx.services.configuration.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public final class FilterConditions {
    private final Properties properties;

    public boolean apiRequest(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/api");
    }

    public boolean cachedApiRequest(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/api/cached");
    }

    public boolean invalidCallerIdOrAuthToken(HttpServletRequest request) {
        return properties.getClients().values()
                .stream()
                .noneMatch(client -> client.getCaller().equals(request.getHeader("X-Caller-Id"))
                                  && client.getToken().equals(request.getHeader("X-Auth-Token")));
    }

    public boolean notPreflightRequest(HttpServletRequest request) {
        return !"OPTIONS".equalsIgnoreCase(request.getMethod());
    }
}