package it.configuration;

import application.SpringApplication;
import it.IntegrationTestBase;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(IntegrationTestBase.class)
@RequiredArgsConstructor
@SpringBootTest(classes = SpringApplication.class)
class WebSecurityIT {
    private final MockMvc mockMvc;

    @Test
    void loginPage_isPubliclyAccessible() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
    }

    @Test
    void rootPath_whenUnauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
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
}