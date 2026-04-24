package it.configuration;

import net.outsideworx.services.SpringApplication;
import it.IntegrationTestBase;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(IntegrationTestBase.class)
@RequiredArgsConstructor
@SpringBootTest(classes = SpringApplication.class)
class CacheFilterIT {
    private final MockMvc mockMvc;

    @Test
    void cachedEndpoint_setsCacheControlHeader() throws Exception {
        mockMvc.perform(get("/api/cached/soupart")
                        .param("category", "art")
                        .param("offset", "0")
                        .header("X-Caller-Id", "soupart")
                        .header("X-Auth-Token", "test"))
                .andExpect(header().exists("Cache-Control"));
    }

    @Test
    void nonCachedEndpoint_doesNotSetPublicCacheControlHeader() throws Exception {
        mockMvc.perform(get("/api/gaiapeeps")
                        .header("X-Caller-Id", "gaiapeeps")
                        .header("X-Auth-Token", "test"))
                .andExpect(header().string("Cache-Control", not(containsString("public"))));
    }

    @Test
    void preflightRequest_doesNotSetPublicCacheControlHeader() throws Exception {
        mockMvc.perform(get("/api/cached/soupart")
                        .param("category", "art")
                        .param("offset", "0")
                        .with(request -> {
                            request.setMethod("OPTIONS");
                            return request;
                        }))
                .andExpect(header().string("Cache-Control", not(containsString("public"))));
    }
}
