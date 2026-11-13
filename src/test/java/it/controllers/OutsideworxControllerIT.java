package it.controllers;

import net.outsideworx.services.SpringApplication;
import net.outsideworx.services.models.clients.worx.TunnelEntity;
import net.outsideworx.services.repositories.clients.TunnelRepository;
import it.IntegrationTestBase;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(IntegrationTestBase.class)
@RequiredArgsConstructor
@SpringBootTest(classes = SpringApplication.class)
class OutsideworxControllerIT {
    private final MockMvc mockMvc;

    private final TunnelRepository tunnelRepository;

    @BeforeEach
    void setUp() {
        tunnelRepository.deleteAll();
    }

    @Test
    void index_whenAuthenticated_rendersConfiguredKnockUrl() throws Exception {
        mockMvc.perform(get("/").with(oidcLogin().idToken(t -> t.claim("email", "user@outsideworx.net"))))
                .andExpect(status().isOk())
                .andExpect(view().name("clients/outsideworx"))
                .andExpect(model().attribute("knockUrl", "https://localhost:8443"))
                .andExpect(content().string(containsString("const KNOCK_URL = \"https://localhost:8443\";")));
    }

    @Test
    void operator_whenAuthenticated_redirectsToKnockUrl() throws Exception {
        mockMvc.perform(get("/operator").with(oidcLogin()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("https://localhost:8443"));
    }

    @Test
    void submit_savesItems() throws Exception {
        mockMvc.perform(post("/outsideworx")
                        .with(oidcLogin())
                        .param("items[0].name", "operator-knock-token")
                        .param("items[0].value", "secret"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        assertThat(tunnelRepository.findAll())
                .extracting(TunnelEntity::getName, TunnelEntity::getValue)
                .containsExactly(tuple("operator-knock-token", "secret"));
    }
}
