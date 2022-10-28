package com.training.apparatus.data.config;

import com.training.apparatus.translation.TranslationProvider;
import com.vaadin.flow.i18n.I18NProvider;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Kulikov Denis
 * @since 20.10.2022
 */

@Slf4j
@Configuration
public class Config {

    @Value("${fasttypping.host}")
    private String mainHost;
    @Value("${spring.mail.host}")
    private String mailHost;

    @Value("${spring.mail.port}")
    private Integer mailPort;
    @Value("${spring.mail.username}")
    private String mailUsername;
    @Value("${spring.mail.password}")
    private String mailPassword;

    @Bean
    public Session mailSession() {


        final String username = mailUsername;
        final String password = mailPassword;
        Properties props = new Properties();

        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", mailHost);
        props.put("mail.smtp.port", mailPort);
        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });
        sendRestartInfo(session, mainHost);
        return session;
    }

    @Bean
    public I18NProvider i18NProvider() {
        return new TranslationProvider();
    }


    public static void sendRestartInfo(Session session, String mainHost) {

        Thread checkSmtp = new Thread(() -> {
            try {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress("support@fasttyping.ru"));
                message.setRecipients(Message.RecipientType.TO,
                        InternetAddress.parse("support@fasttyping.ru"));
                message.setSubject("Restart " + mainHost);
                message.setText("Restart stand ");

                Transport.send(message);
                log.info("Send restart message");
            } catch (MessagingException e) {
                log.error("Error in initiate mail", e);
            }
        });
        checkSmtp.start();
    }


}
