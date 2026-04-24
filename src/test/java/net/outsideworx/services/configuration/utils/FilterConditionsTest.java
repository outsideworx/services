package net.outsideworx.services.configuration.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FilterConditionsTest {
    @Mock
    private Properties properties;

    @InjectMocks
    private FilterConditions filterConditions;

    private final Properties.Client validClient = new Properties.Client();

    @BeforeEach
    void setUp() {
        validClient.setCaller("client1");
        validClient.setToken("secret");
    }

    @Test
    void apiRequest_whenUriStartsWithApi_returnsTrue() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/something");
        assertThat(filterConditions.apiRequest(request)).isTrue();
    }

    @Test
    void apiRequest_whenUriDoesNotStartWithApi_returnsFalse() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/other");
        assertThat(filterConditions.apiRequest(request)).isFalse();
    }

    @Test
    void cachedApiRequest_whenUriStartsWithApiCached_returnsTrue() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/cached/something");
        assertThat(filterConditions.cachedApiRequest(request)).isTrue();
    }

    @Test
    void cachedApiRequest_whenUriDoesNotStartWithApiCached_returnsFalse() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/something");
        assertThat(filterConditions.cachedApiRequest(request)).isFalse();
    }

    @Test
    void notPreflightRequest_whenMethodIsOptions_returnsFalse() {
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api");
        assertThat(filterConditions.notPreflightRequest(request)).isFalse();
    }

    @Test
    void notPreflightRequest_whenMethodIsGet_returnsTrue() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api");
        assertThat(filterConditions.notPreflightRequest(request)).isTrue();
    }

    @Test
    void invalidCallerIdOrAuthToken_whenHeadersMatch_returnsFalse() {
        when(properties.getClients()).thenReturn(Map.of("client1", validClient));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Caller-Id", "client1");
        request.addHeader("X-Auth-Token", "secret");
        assertThat(filterConditions.invalidCallerIdOrAuthToken(request)).isFalse();
    }

    @Test
    void invalidCallerIdOrAuthToken_whenCallerIdDoesNotMatch_returnsTrue() {
        when(properties.getClients()).thenReturn(Map.of("client1", validClient));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Caller-Id", "wrong");
        request.addHeader("X-Auth-Token", "secret");
        assertThat(filterConditions.invalidCallerIdOrAuthToken(request)).isTrue();
    }

    @Test
    void invalidCallerIdOrAuthToken_whenTokenDoesNotMatch_returnsTrue() {
        when(properties.getClients()).thenReturn(Map.of("client1", validClient));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Caller-Id", "client1");
        request.addHeader("X-Auth-Token", "wrong");
        assertThat(filterConditions.invalidCallerIdOrAuthToken(request)).isTrue();
    }

    @Test
    void invalidCallerIdOrAuthToken_whenClientsMapIsEmpty_returnsTrue() {
        when(properties.getClients()).thenReturn(Map.of());
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Caller-Id", "client1");
        request.addHeader("X-Auth-Token", "secret");
        assertThat(filterConditions.invalidCallerIdOrAuthToken(request)).isTrue();
    }
}
