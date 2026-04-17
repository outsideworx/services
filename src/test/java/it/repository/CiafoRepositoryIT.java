package it.repository;

import application.SpringApplication;
import application.model.clients.ciafo.CiafoEntity;
import application.model.clients.ciafo.mapping.CiafoPayload;
import application.repository.clients.CiafoRepository;
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
class CiafoRepositoryIT {
    private final CiafoRepository ciafoRepository;

    @BeforeEach
    void setUp() {
        ciafoRepository.deleteAll();
    }

    private CiafoEntity entity(String category, String description, String image1) {
        CiafoEntity e = new CiafoEntity();
        e.setCategory(category);
        e.setDescription(description);
        e.setImage1(image1);
        return e;
    }

    @Test
    void getThumbnails_returnsOnlyMatchingCategory() {
        ciafoRepository.save(entity("Furniture", "Chair", null));
        ciafoRepository.save(entity("Jewelry", "Ring", null));

        assertThat(ciafoRepository.getThumbnails("Furniture"))
                .hasSize(1)
                .allMatch(t -> "Furniture".equals(t.getCategory()));
    }

    @Test
    void getThumbnails_whenNoneMatch_returnsEmptyList() {
        ciafoRepository.save(entity("Furniture", "Chair", null));

        assertThat(ciafoRepository.getThumbnails("Jewelry")).isEmpty();
    }

    @Test
    void getPreviews_respectsLimitAndOffset() {
        for (int i = 0; i < 8; i++) {
            ciafoRepository.save(entity("Furniture", "Item " + i, null));
        }

        assertThat(ciafoRepository.getPreviews("Furniture", 0)).hasSize(6);
        assertThat(ciafoRepository.getPreviews("Furniture", 6)).hasSize(2);
    }

    @Test
    void getPreviews_whenOffsetExceedsTotal_returnsEmptyList() {
        ciafoRepository.save(entity("Furniture", "Chair", null));

        assertThat(ciafoRepository.getPreviews("Furniture", 10)).isEmpty();
    }

    @Test
    void get_returnsCorrectEntity() {
        CiafoEntity saved = ciafoRepository.save(entity("Furniture", "Chair", "data:image/jpeg;base64,abc"));

        CiafoPayload result = ciafoRepository.get(saved.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(saved.getId());
        assertThat(result.getDescription()).isEqualTo("Chair");
        assertThat(result.getImage1()).isEqualTo("data:image/jpeg;base64,abc");
    }

    @Test
    void get_whenIdDoesNotExist_returnsNull() {
        assertThat(ciafoRepository.get(999L)).isNull();
    }

    @Test
    void update_overwritesNonNullFields() {
        CiafoEntity saved = ciafoRepository.save(entity("Furniture", "Old", "old-image"));

        saved.setDescription("New");
        saved.setImage1("new-image");
        ciafoRepository.update(saved);

        CiafoPayload result = ciafoRepository.get(saved.getId());
        assertThat(result.getDescription()).isEqualTo("New");
        assertThat(result.getImage1()).isEqualTo("new-image");
    }

    @Test
    void update_whenImageIsNull_doesNotOverwriteExistingImage() {
        CiafoEntity saved = ciafoRepository.save(entity("Furniture", "Chair", "existing-image"));

        saved.setDescription("Updated");
        saved.setImage1(null);
        ciafoRepository.update(saved);

        CiafoPayload result = ciafoRepository.get(saved.getId());
        assertThat(result.getImage1()).isEqualTo("existing-image");
    }
}