package com.training.apparatus.data.service;

import com.training.apparatus.data.entity.User;
import com.training.apparatus.data.repo.UserRepository;
import com.vaadin.flow.i18n.I18NProvider;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmailServiceImpl  {

    @Value("${spring.mail.fromEmail}")
    private String fromEmail;

    @Value("${fasttypping.host}")
    private String host;

    @Autowired
    private Session mailSession;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EncodingService encodingService;

    @Autowired
    private I18NProvider i18NProvider;

    private final ExecutorService executorService = Executors.newFixedThreadPool(1);


    public void sendSimpleMessage(
      String to, String subject, String text) {

        try {
            Message message = new MimeMessage(mailSession);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(text);
            Transport.send(message);
            log.info("Sent message to {} with subject {}", to, subject);
        } catch (MessagingException e) {
            log.error("Error in sending message", e);
        }
    }

    public void restorePassword(String email, Locale locale, Runnable runnable) {

        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("Not found user by email" + email );
        }

        String hashCode = encodingService.encodingRestoreEmail(email, user.getId());
        String url = "%s/restorePassword?email=%s&hash=%s".formatted(host, email, hashCode);
        executorService.submit(() -> {
            sendSimpleMessage(email, i18NProvider.getTranslation("restore.emailSubject", locale), i18NProvider.getTranslation("restore.text", locale, user.getPseudonym(), host, url));
            runnable.run();
        });


    }

    public void approveEmail(User user, Locale locale, Runnable runnable) {
        String hashCode = encodingService.encodingRestoreEmail(user.getEmail(), user.getId());
        String url = "%s/approveEmail?email=%s&hash=%s".formatted(host, user.getEmail(), hashCode);
        executorService.submit(() -> {
            sendSimpleMessage(user.getEmail(), i18NProvider.getTranslation("approve.emailSubject", locale), i18NProvider.getTranslation("approve.text", locale, host, url));
            runnable.run();
        });

    }
}