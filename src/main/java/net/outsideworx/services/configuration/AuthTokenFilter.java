package net.outsideworx.services.configuration;

import net.outsideworx.services.configuration.utils.FilterConditions;
import net.outsideworx.services.gateway.GrafanaGateway;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
final class AuthTokenFilter extends HttpFilter {
    private final FilterConditions filterConditions;

    private final GrafanaGateway grafanaGateway;

    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (filterConditions.notPreflightRequest(request) && filterConditions.apiRequest(request)) {
            if (filterConditions.invalidCallerIdOrAuthToken(request)) {
                log.error("Invalid callerId or authToken for request: [{}]", request.getRequestURL());
                grafanaGateway.registerException("bad_credentials");
                throw new BadCredentialsException("Invalid caller id or auth token.");
            }
        }
        chain.doFilter(request, response);
    }
}