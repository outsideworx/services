package it.controller;

import application.SpringApplication;
import application.model.CallbackEntity;
import application.repository.CallbackRepository;
import application.service.EmailService;
import com.mailersend.sdk.exceptions.MailerSendException;
import it.IntegrationTestBase;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(IntegrationTestBase.class)
@RequiredArgsConstructor
@SpringBootTest(classes = SpringApplication.class)
class CallbackControllerIT {
    private static final String VALID_BODY = """
            {"address":"visitor@example.com","product":"https://example.com/product"}
            """;

    private final CallbackRepository callbackRepository;

    @MockitoBean
    private EmailService emailService;

    private final MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        callbackRepository.deleteAll();
    }

    @Test
    void postCallback_withMissingAuthHeaders_redirectsToLogin() throws Exception {
        mockMvc.perform(post("/api/callback")
                        .contentType("application/json")
                        .content(VALID_BODY))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void postCallback_withWrongToken_redirectsToLogin() throws Exception {
        mockMvc.perform(post("/api/callback")
                        .header("X-Caller-Id", "come-in-and-find-out")
                        .header("X-Auth-Token", "wrong")
                        .contentType("application/json")
                        .content(VALID_BODY))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void postCallback_withValidCredentials_returnsOk() throws Exception {
        mockMvc.perform(post("/api/callback")
                        .header("X-Caller-Id", "come-in-and-find-out")
                        .header("X-Auth-Token", "test")
                        .contentType("application/json")
                        .content(VALID_BODY))
                .andExpect(status().isOk());
    }

    @Test
    void postCallback_persistsEntityToDatabase() throws Exception {
        mockMvc.perform(post("/api/callback")
                        .header("X-Caller-Id", "come-in-and-find-out")
                        .header("X-Auth-Token", "test")
                        .contentType("application/json")
                        .content(VALID_BODY));

        List<CallbackEntity> saved = StreamSupport
                .stream(callbackRepository.findAll().spliterator(), false)
                .toList();
        assertThat(saved).hasSize(1);
        assertThat(saved.getFirst().getAddress()).isEqualTo("visitor@example.com");
        assertThat(saved.getFirst().getProduct()).isEqualTo("https://example.com/product");
        assertThat(saved.getFirst().getRecipient()).isEqualTo("come-in-and-find-out");
    }

    @Test
    void postCallback_sendsEmailWithCallerId() throws Exception {
        mockMvc.perform(post("/api/callback")
                        .header("X-Caller-Id", "come-in-and-find-out")
                        .header("X-Auth-Token", "test")
                        .contentType("application/json")
                        .content(VALID_BODY));

        verify(emailService).send(eq("come-in-and-find-out"), any(), any());
    }

    @Test
    void postCallback_whenEmailFails_stillPersistsEntity() throws Exception {
        doThrow(new MailerSendException("send failed")).when(emailService).send(any(), any(), any());

        try {
            mockMvc.perform(post("/api/callback")
                    .header("X-Caller-Id", "come-in-and-find-out")
                    .header("X-Auth-Token", "test")
                    .contentType("application/json")
                    .content(VALID_BODY));
        } catch (Exception ignored) {}

        List<CallbackEntity> saved = StreamSupport
                .stream(callbackRepository.findAll().spliterator(), false)
                .toList();
        assertThat(saved).hasSize(1);
        assertThat(saved.getFirst().getAddress()).isEqualTo("visitor@example.com");
        assertThat(saved.getFirst().getProduct()).isEqualTo("https://example.com/product");
        assertThat(saved.getFirst().getRecipient()).isEqualTo("come-in-and-find-out");
    }
}