package application.service;

import com.mailersend.sdk.emails.Email;
import com.mailersend.sdk.MailerSend;
import com.mailersend.sdk.exceptions.MailerSendException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public final class EmailService {
    @Value("${mailersend.sdk.token:#{null}}")
    private String token;

    public void send(String callerId, String subject, String text) throws MailerSendException {
        Email email = new Email();
        email.setFrom("Outside Worx", "info@outsideworx.net");
        email.addRecipient(null, callerId.concat("@outsideworx.net"));
        email.setSubject(subject);
        email.setHtml(text);
        MailerSend mailerSend = new MailerSend();
        mailerSend.setToken(token);
        log.info("Mail sent wih status code: [{}]", mailerSend.emails().send(email).responseStatusCode);
    }
}