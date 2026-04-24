package it.controller;

import net.outsideworx.services.SpringApplication;
import net.outsideworx.services.model.clients.peeps.PeepsEntity;
import net.outsideworx.services.repository.clients.PeepsRepository;
import it.IntegrationTestBase;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(IntegrationTestBase.class)
@RequiredArgsConstructor
@SpringBootTest(classes = SpringApplication.class)
class PeepsControllerIT {
    private final MockMvc mockMvc;

    private final PeepsRepository peepsRepository;

    @BeforeEach
    void setUp() {
        peepsRepository.deleteAll();
    }

    private PeepsEntity entity(String title, String link) {
        PeepsEntity e = new PeepsEntity();
        e.setTitle(title);
        e.setLink(link);
        return e;
    }

    @Test
    void getItems_withMissingAuthHeaders_redirectsToOAuth2Login() throws Exception {
        mockMvc.perform(get("/api/gaiapeeps"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/oauth2/authorization/authelia"));
    }

    @Test
    void getItems_withWrongToken_redirectsToOAuth2Login() throws Exception {
        mockMvc.perform(get("/api/gaiapeeps")
                        .header("X-Caller-Id", "gaiapeeps")
                        .header("X-Auth-Token", "wrong"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/oauth2/authorization/authelia"));
    }

    @Test
    void getItems_withValidCredentials_returnsOk() throws Exception {
        mockMvc.perform(get("/api/gaiapeeps")
                        .header("X-Caller-Id", "gaiapeeps")
                        .header("X-Auth-Token", "test"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"));
    }

    @Test
    void getItems_returnsAllItems() throws Exception {
        peepsRepository.save(entity("Artist One", "https://example.com/1"));
        peepsRepository.save(entity("Artist Two", "https://example.com/2"));

        mockMvc.perform(get("/api/gaiapeeps")
                        .header("X-Caller-Id", "gaiapeeps")
                        .header("X-Auth-Token", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Artist One"))
                .andExpect(jsonPath("$[1].title").value("Artist Two"));
    }

    @Test
    void getItems_returnsTitleAndLink() throws Exception {
        peepsRepository.save(entity("Artist One", "https://example.com/1"));

        mockMvc.perform(get("/api/gaiapeeps")
                        .header("X-Caller-Id", "gaiapeeps")
                        .header("X-Auth-Token", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Artist One"))
                .andExpect(jsonPath("$[0].link").value("https://example.com/1"));
    }

    @Test
    void getItems_whenNoneExist_returnsEmptyArray() throws Exception {
        mockMvc.perform(get("/api/gaiapeeps")
                        .header("X-Caller-Id", "gaiapeeps")
                        .header("X-Auth-Token", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}