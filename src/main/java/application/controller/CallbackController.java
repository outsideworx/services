package application.controller;

import application.model.Callback;
import application.model.CallbackEntity;
import application.repository.CallbackRepository;
import application.service.EmailService;
import com.mailersend.sdk.exceptions.MailerSendException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin({"${app.clients.ciafo.origin}"})
@RestController
@RequiredArgsConstructor
@Slf4j
final class CallbackController {
    private final CallbackRepository callbackRepository;

    private final EmailService emailService;

    @PostMapping("/api/callback")
    void callback(@RequestHeader("X-Caller-Id") String callerId, @RequestBody Callback callback) {
        log.info("Callback received for: [{}], with payload: [{}]", callerId, callback);
        try {
            emailService.send(
                    callerId,
                    "Someone is interested!",
                    String.format(
                            "A visitor left the following contact: %s.<br>The product he was interested in is: <a href=%s>this</a>.",
                            callback.getAddress(),
                            callback.getProduct()));
        } catch (MailerSendException | NullPointerException e) {
            throw new IllegalStateException("Email sending failed.", e);
        } finally {
            CallbackEntity callbackEntity = new CallbackEntity();
            callbackEntity.setAddress(callback.getAddress());
            callbackEntity.setProduct(callback.getProduct());
            callbackEntity.setRecipient(callerId);
            callbackRepository.save(callbackEntity);
            log.info("Callback saved with id: [{}]", callbackEntity.getId());
        }
    }
}