package it.controller;

import application.SpringApplication;
import application.model.clients.soup.SoupEntity;
import application.repository.clients.SoupRepository;
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
class SoupControllerIT {
    private final MockMvc mockMvc;

    private final SoupRepository soupRepository;

    @BeforeEach
    void setUp() {
        soupRepository.deleteAll();
    }

    private SoupEntity entity(String category, String description, String image, String link) {
        SoupEntity e = new SoupEntity();
        e.setCategory(category);
        e.setDescription(description);
        e.setImage(image);
        e.setLink(link);
        return e;
    }

    @Test
    void getItems_withMissingAuthHeaders_redirectsToOAuth2Login() throws Exception {
        mockMvc.perform(get("/api/cached/soupart")
                        .param("category", "art")
                        .param("offset", "0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/oauth2/authorization/authelia"));
    }

    @Test
    void getItems_withWrongToken_redirectsToOAuth2Login() throws Exception {
        mockMvc.perform(get("/api/cached/soupart")
                        .param("category", "art")
                        .param("offset", "0")
                        .header("X-Caller-Id", "soupart")
                        .header("X-Auth-Token", "wrong"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/oauth2/authorization/authelia"));
    }

    @Test
    void getItems_withValidCredentials_returnsOk() throws Exception {
        mockMvc.perform(get("/api/cached/soupart")
                        .param("category", "art")
                        .param("offset", "0")
                        .header("X-Caller-Id", "soupart")
                        .header("X-Auth-Token", "test"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"));
    }

    @Test
    void getItems_returnsOnlyMatchingCategory() throws Exception {
        soupRepository.save(entity("art", "Painting", "img1", "https://example.com/1"));
        soupRepository.save(entity("design", "Poster", "img2", "https://example.com/2"));

        mockMvc.perform(get("/api/cached/soupart")
                        .param("category", "art")
                        .param("offset", "0")
                        .header("X-Caller-Id", "soupart")
                        .header("X-Auth-Token", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].description").value("Painting"))
                .andExpect(jsonPath("$[0].image").value("img1"))
                .andExpect(jsonPath("$[0].link").value("https://example.com/1"));
    }

    @Test
    void getItems_withOffset_returnsCorrectPage() throws Exception {
        for (int i = 1; i <= 11; i++) {
            soupRepository.save(entity("art", "Item " + i, "img" + i, null));
        }

        mockMvc.perform(get("/api/cached/soupart")
                        .param("category", "art")
                        .param("offset", "9")
                        .header("X-Caller-Id", "soupart")
                        .header("X-Auth-Token", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getItems_whenNoneMatch_returnsEmptyArray() throws Exception {
        mockMvc.perform(get("/api/cached/soupart")
                        .param("category", "art")
                        .param("offset", "0")
                        .header("X-Caller-Id", "soupart")
                        .header("X-Auth-Token", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}