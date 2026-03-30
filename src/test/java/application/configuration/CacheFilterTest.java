package application.configuration;

import application.configuration.utils.FilterConditions;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CacheFilterTest {

    @Mock
    private FilterConditions filterConditions;

    @Mock
    private FilterChain chain;

    @InjectMocks
    private CacheFilter cacheFilter;

    @Test
    void doFilter_whenCachedApiRequest_setsCacheControlHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(filterConditions.notPreflightRequest(request)).thenReturn(true);
        when(filterConditions.cachedApiRequest(request)).thenReturn(true);

        cacheFilter.doFilter(request, response, chain);

        assertThat(response.getHeader("Cache-Control")).isEqualTo("public, max-age=86400");
        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilter_whenNotCachedApiRequest_doesNotSetCacheControlHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(filterConditions.notPreflightRequest(request)).thenReturn(true);
        when(filterConditions.cachedApiRequest(request)).thenReturn(false);

        cacheFilter.doFilter(request, response, chain);

        assertThat(response.getHeader("Cache-Control")).isNull();
        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilter_whenPreflightRequest_doesNotSetCacheControlHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(filterConditions.notPreflightRequest(request)).thenReturn(false);

        cacheFilter.doFilter(request, response, chain);

        assertThat(response.getHeader("Cache-Control")).isNull();
        verify(chain).doFilter(request, response);
    }
}
