package net.outsideworx.services.configuration;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LogbackFilterTest {
    @Mock
    private FilterChain chain;

    @InjectMocks
    private LogbackFilter logbackFilter;

    @Test
    void doFilter_whenRequestIdHeaderPresent_usesThatRequestId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Request-Id", "my-request-id");
        MockHttpServletResponse response = new MockHttpServletResponse();

        logbackFilter.doFilter(request, response, chain);

        assertThat(MDC.get("requestId")).isEqualTo("my-request-id");
        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilter_whenRequestIdHeaderMissing_generatesRequestId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        logbackFilter.doFilter(request, response, chain);

        assertThat(MDC.get("requestId")).isNotBlank().hasSize(26);
        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilter_whenRequestIdHeaderIsBlank_generatesRequestId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Request-Id", "   ");
        MockHttpServletResponse response = new MockHttpServletResponse();

        logbackFilter.doFilter(request, response, chain);

        assertThat(MDC.get("requestId")).isNotBlank().hasSize(26);
        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilter_whenRequestIdHeaderMissing_generatedIdIsUrlSafeBase64() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        logbackFilter.doFilter(request, response, chain);

        assertThat(MDC.get("requestId")).matches("[A-Za-z0-9_-]{26}");
    }
}