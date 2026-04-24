package it.repository;

import net.outsideworx.services.SpringApplication;
import net.outsideworx.services.model.clients.peeps.PeepsEntity;
import net.outsideworx.services.repository.clients.PeepsRepository;
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
class PeepsRepositoryIT {
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
    void save_persistsPeepsEntity() {
        PeepsEntity saved = peepsRepository.save(entity("Artist One", "https://example.com/1"));

        assertThat(saved.getId()).isNotNull();
        assertThat(peepsRepository.findById(saved.getId())).isPresent()
                .hasValueSatisfying(found -> {
                    assertThat(found.getTitle()).isEqualTo("Artist One");
                    assertThat(found.getLink()).isEqualTo("https://example.com/1");
                });
    }

    @Test
    void delete_removesPeepsEntity() {
        PeepsEntity saved = peepsRepository.save(entity("Artist One", "https://example.com/1"));

        peepsRepository.deleteById(saved.getId());

        assertThat(peepsRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    void findAll_returnsAllSavedEntities() {
        peepsRepository.save(entity("Artist One", "https://example.com/1"));
        peepsRepository.save(entity("Artist Two", "https://example.com/2"));

        assertThat(peepsRepository.findAll()).hasSize(2)
                .extracting(PeepsEntity::getTitle)
                .containsExactlyInAnyOrder("Artist One", "Artist Two");
    }
}
