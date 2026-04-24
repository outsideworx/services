package it.repository;

import net.outsideworx.services.SpringApplication;
import net.outsideworx.services.model.CallbackEntity;
import net.outsideworx.services.repository.CallbackRepository;
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
class CallbackRepositoryIT {
    private final CallbackRepository callbackRepository;

    @BeforeEach
    void setUp() {
        callbackRepository.deleteAll();
    }

    @Test
    void save_persistsCallbackEntity() {
        CallbackEntity entity = new CallbackEntity();
        entity.setAddress("test@example.com");
        entity.setProduct("https://example.com/product");
        entity.setRecipient("come-in-and-find-out");

        CallbackEntity saved = callbackRepository.save(entity);

        assertThat(saved.getId()).isNotNull();
        assertThat(callbackRepository.findById(saved.getId())).isPresent()
                .hasValueSatisfying(found -> {
                    assertThat(found.getAddress()).isEqualTo("test@example.com");
                    assertThat(found.getProduct()).isEqualTo("https://example.com/product");
                    assertThat(found.getRecipient()).isEqualTo("come-in-and-find-out");
                });
    }

    @Test
    void delete_removesCallbackEntity() {
        CallbackEntity entity = new CallbackEntity();
        entity.setAddress("delete@example.com");
        entity.setRecipient("come-in-and-find-out");
        CallbackEntity saved = callbackRepository.save(entity);

        callbackRepository.deleteById(saved.getId());

        assertThat(callbackRepository.findById(saved.getId())).isEmpty();
    }
}