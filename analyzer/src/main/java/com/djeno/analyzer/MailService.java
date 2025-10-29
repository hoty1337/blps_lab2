package com.djeno.analyzer;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Named
@Slf4j
public class MailService {

    @Resource(mappedName = "java:jboss/mail/Default")
    private Session mailSession;

    public void sendReport(String to, String subject, String bodyText) {
        try {
            MimeMessage msg = new MimeMessage(mailSession);
            mailSession.setDebug(true);
            msg.setFrom(new InternetAddress("iiec1337@gmail.com", "Static Analysis"));
            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
            msg.setSubject(subject, "UTF-8");
            msg.setText(bodyText, "UTF-8");
            Transport.send(msg);
            log.info("[MailService] Письмо успешно отправлено " + to);
        } catch (Exception e) {
            log.error("[MailService] Ошибка при отправке письма: " + e.getMessage());
        }
    }
}