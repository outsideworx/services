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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(IntegrationTestBase.class)
@RequiredArgsConstructor
@SpringBootTest(classes = SpringApplication.class)
class WebSecurityIT {
    private final MockMvc mockMvc;

    @Test
    void rootPath_whenUnauthenticated_redirectsToOAuth2Login() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/oauth2/authorization/authelia"));
    }

    @Test
    void rootPath_whenAuthenticatedWithKnownDomain_rendersClientView() throws Exception {
        mockMvc.perform(get("/").with(oidcLogin().idToken(t -> t.claim("email", "user@gaiapeeps.com"))))
                .andExpect(status().isOk())
                .andExpect(view().name("clients/gaiapeeps"));
    }

    @Test
    void rootPath_whenAuthenticatedWithUnknownDomain_returnsForbidden() throws Exception {
        mockMvc.perform(get("/").with(oidcLogin().idToken(t -> t.claim("email", "user@unknown.com"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void rootPath_whenAuthenticatedWithMalformedEmail_returnsForbidden() throws Exception {
        mockMvc.perform(get("/").with(oidcLogin().idToken(t -> t.claim("email", "notanemail"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void apiPath_whenUnauthenticated_isNotRedirectedToOAuth2Login() throws Exception {
        mockMvc.perform(get("/api/gaiapeeps")
                        .header("X-Caller-Id", "gaiapeeps")
                        .header("X-Auth-Token", "test"))
                .andExpect(status().isOk());
    }

    @Test
    void actuatorPrometheus_isOnSeparateManagementPort() throws Exception {
        mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isNotFound());
    }

    @Test
    void robotsTxt_isPubliclyAccessible() throws Exception {
        mockMvc.perform(get("/robots.txt"))
                .andExpect(status().isOk());
    }

    @Test
    void sitemapXml_isPubliclyAccessible() throws Exception {
        mockMvc.perform(get("/sitemap.xml"))
                .andExpect(status().isOk());
    }

    @Test
    void grafana_isPubliclyAccessible() throws Exception {
        mockMvc.perform(get("/grafana"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void ntfy_isPubliclyAccessible() throws Exception {
        mockMvc.perform(get("/ntfy"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void imgPath_isPubliclyAccessible() throws Exception {
        mockMvc.perform(get("/img/favicon.ico"))
                .andExpect(status().isOk());
    }

    @Test
    void adminPost_whenUnauthenticated_redirectsToOAuth2Login() throws Exception {
        mockMvc.perform(post("/gaiapeeps"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/oauth2/authorization/authelia"));
    }
}