package it.repository;

import application.SpringApplication;
import application.model.clients.soup.SoupEntity;
import application.repository.clients.SoupRepository;
import it.IntegrationTestBase;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@Import(IntegrationTestBase.class)
@RequiredArgsConstructor
@SpringBootTest(classes = SpringApplication.class)
class SoupRepositoryIT {
    private final SoupRepository soupRepository;

    @BeforeEach
    void setUp() {
        soupRepository.deleteAll();
    }

    private SoupEntity entity(String category, String description, String image) {
        SoupEntity e = new SoupEntity();
        e.setCategory(category);
        e.setDescription(description);
        e.setImage(image);
        return e;
    }

    @Test
    void get_returnsOnlyMatchingCategory() {
        soupRepository.save(entity("art", "Painting", null));
        soupRepository.save(entity("design", "Poster", null));

        assertThat(soupRepository.get("art"))
                .hasSize(1)
                .first()
                .satisfies(s -> {
                    assertThat(s.getCategory()).isEqualTo("art");
                    assertThat(s.getDescription()).isEqualTo("Painting");
                });
    }

    @Test
    void get_whenNoneMatch_returnsEmptyList() {
        soupRepository.save(entity("art", "Painting", null));

        assertThat(soupRepository.get("design")).isEmpty();
    }

    @Test
    void getWithOffset_respectsLimitAndOffset() {
        for (int i = 0; i < 11; i++) {
            soupRepository.save(entity("art", "Item " + i, null));
        }

        assertThat(soupRepository.get("art", 0)).hasSize(9);
        assertThat(soupRepository.get("art", 9)).hasSize(2);
    }

    @Test
    void getWithOffset_whenOffsetExceedsTotal_returnsEmptyList() {
        soupRepository.save(entity("art", "Painting", null));

        assertThat(soupRepository.get("art", 10)).isEmpty();
    }

    @Test
    void update_overwritesNonNullFields() {
        SoupEntity saved = soupRepository.save(entity("art", "Old", "old-image"));

        saved.setDescription("New");
        saved.setImage("new-image");
        soupRepository.update(saved);

        SoupEntity result = soupRepository.findById(saved.getId()).orElseThrow();
        assertThat(result.getDescription()).isEqualTo("New");
        assertThat(result.getImage()).isEqualTo("new-image");
    }

    @Test
    void update_whenImageIsNull_doesNotOverwriteExistingImage() {
        SoupEntity saved = soupRepository.save(entity("art", "Painting", "existing-image"));

        saved.setDescription("Updated");
        saved.setImage(null);
        soupRepository.update(saved);

        SoupEntity result = soupRepository.findById(saved.getId()).orElseThrow();
        assertThat(result.getImage()).isEqualTo("existing-image");
    }
}