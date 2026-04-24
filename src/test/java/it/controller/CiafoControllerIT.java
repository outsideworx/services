package it.controller;

import net.outsideworx.services.SpringApplication;
import net.outsideworx.services.model.clients.ciafo.CiafoEntity;
import net.outsideworx.services.repository.clients.CiafoRepository;
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
class CiafoControllerIT {
    private final MockMvc mockMvc;

    private final CiafoRepository ciafoRepository;

    @BeforeEach
    void setUp() {
        ciafoRepository.deleteAll();
    }

    private CiafoEntity entity(String category, String description, String image1, String image2, String thumbnail1) {
        CiafoEntity e = new CiafoEntity();
        e.setCategory(category);
        e.setDescription(description);
        e.setImage1(image1);
        e.setImage2(image2);
        e.setThumbnail1(thumbnail1);
        return e;
    }

    @Test
    void getPreviews_withMissingAuthHeaders_redirectsToOAuth2Login() throws Exception {
        mockMvc.perform(get("/api/come-in-and-find-out")
                        .param("category", "Furniture")
                        .param("offset", "0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/oauth2/authorization/authelia"));
    }

    @Test
    void getPreviews_withWrongToken_redirectsToOAuth2Login() throws Exception {
        mockMvc.perform(get("/api/come-in-and-find-out")
                        .param("category", "Furniture")
                        .param("offset", "0")
                        .header("X-Caller-Id", "come-in-and-find-out")
                        .header("X-Auth-Token", "wrong"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/oauth2/authorization/authelia"));
    }

    @Test
    void getPreviews_withValidCredentials_returnsOk() throws Exception {
        mockMvc.perform(get("/api/come-in-and-find-out")
                        .param("category", "Furniture")
                        .param("offset", "0")
                        .header("X-Caller-Id", "come-in-and-find-out")
                        .header("X-Auth-Token", "test"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"));
    }

    @Test
    void getPreviews_returnsOnlyMatchingCategory() throws Exception {
        ciafoRepository.save(entity("Furniture", "Chair", "img1", null, "thumb1"));
        ciafoRepository.save(entity("Jewelry", "Ring", "img2", null, "thumb2"));

        mockMvc.perform(get("/api/come-in-and-find-out")
                        .param("category", "Furniture")
                        .param("offset", "0")
                        .header("X-Caller-Id", "come-in-and-find-out")
                        .header("X-Auth-Token", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].description").value("Chair"))
                .andExpect(jsonPath("$[0].image1").value("img1"));
    }

    @Test
    void getPreviews_withOffset_returnsCorrectPage() throws Exception {
        for (int i = 1; i <= 8; i++) {
            ciafoRepository.save(entity("Furniture", "Item " + i, "img" + i, null, null));
        }

        mockMvc.perform(get("/api/come-in-and-find-out")
                        .param("category", "Furniture")
                        .param("offset", "6")
                        .header("X-Caller-Id", "come-in-and-find-out")
                        .header("X-Auth-Token", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getPreviews_whenNoneMatch_returnsEmptyArray() throws Exception {
        mockMvc.perform(get("/api/come-in-and-find-out")
                        .param("category", "Furniture")
                        .param("offset", "0")
                        .header("X-Caller-Id", "come-in-and-find-out")
                        .header("X-Auth-Token", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getPayload_withMissingAuthHeaders_redirectsToOAuth2Login() throws Exception {
        mockMvc.perform(get("/api/cached/come-in-and-find-out")
                        .param("id", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/oauth2/authorization/authelia"));
    }

    @Test
    void getPayload_withWrongToken_redirectsToOAuth2Login() throws Exception {
        mockMvc.perform(get("/api/cached/come-in-and-find-out")
                        .param("id", "1")
                        .header("X-Caller-Id", "come-in-and-find-out")
                        .header("X-Auth-Token", "wrong"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/oauth2/authorization/authelia"));
    }

    @Test
    void getPayload_withValidCredentials_returnsOk() throws Exception {
        CiafoEntity saved = ciafoRepository.save(entity("Furniture", "Chair", "img1", null, null));

        mockMvc.perform(get("/api/cached/come-in-and-find-out")
                        .param("id", saved.getId().toString())
                        .header("X-Caller-Id", "come-in-and-find-out")
                        .header("X-Auth-Token", "test"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"));
    }

    @Test
    void getPayload_returnsAllImageFields() throws Exception {
        CiafoEntity saved = ciafoRepository.save(entity("Furniture", "Chair", "img1", "img2", null));

        mockMvc.perform(get("/api/cached/come-in-and-find-out")
                        .param("id", saved.getId().toString())
                        .header("X-Caller-Id", "come-in-and-find-out")
                        .header("X-Auth-Token", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Chair"))
                .andExpect(jsonPath("$.image1").value("img1"))
                .andExpect(jsonPath("$.image2").value("img2"));
    }

    @Test
    void getPayload_whenIdDoesNotExist_returnsNull() throws Exception {
        mockMvc.perform(get("/api/cached/come-in-and-find-out")
                        .param("id", "999")
                        .header("X-Caller-Id", "come-in-and-find-out")
                        .header("X-Auth-Token", "test"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }
}