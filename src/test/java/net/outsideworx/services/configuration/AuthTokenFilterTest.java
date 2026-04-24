package net.outsideworx.services.configuration;

import net.outsideworx.services.configuration.utils.FilterConditions;
import net.outsideworx.services.service.GrafanaService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthTokenFilterTest {
    @Mock
    private FilterConditions filterConditions;

    @Mock
    private GrafanaService grafanaService;

    @Mock
    private FilterChain chain;

    @InjectMocks
    private AuthTokenFilter authTokenFilter;

    @Test
    void doFilter_whenValidApiRequest_chainsThrough() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(filterConditions.notPreflightRequest(request)).thenReturn(true);
        when(filterConditions.apiRequest(request)).thenReturn(true);
        when(filterConditions.invalidCallerIdOrAuthToken(request)).thenReturn(false);

        authTokenFilter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verifyNoInteractions(grafanaService);
    }

    @Test
    void doFilter_whenInvalidCredentials_throwsBadCredentialsException() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(filterConditions.notPreflightRequest(request)).thenReturn(true);
        when(filterConditions.apiRequest(request)).thenReturn(true);
        when(filterConditions.invalidCallerIdOrAuthToken(request)).thenReturn(true);

        assertThatThrownBy(() -> authTokenFilter.doFilter(request, response, chain))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid caller id or auth token.");
        verify(grafanaService).registerException("bad_credentials");
    }

    @Test
    void doFilter_whenPreflightRequest_skipsAuthAndChainsThrough() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(filterConditions.notPreflightRequest(request)).thenReturn(false);

        authTokenFilter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(filterConditions, never()).invalidCallerIdOrAuthToken(any());
    }

    @Test
    void doFilter_whenNonApiRequest_skipsAuthAndChainsThrough() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(filterConditions.notPreflightRequest(request)).thenReturn(true);
        when(filterConditions.apiRequest(request)).thenReturn(false);

        authTokenFilter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(filterConditions, never()).invalidCallerIdOrAuthToken(any());
    }
}