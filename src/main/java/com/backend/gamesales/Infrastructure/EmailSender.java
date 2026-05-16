package com.backend.gamesales.Infrastructure;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class EmailSender {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    public void sendSimpleMessage(MailBody mailBody) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setTo(mailBody.to());
            helper.setFrom(fromEmail);
            helper.setSubject(mailBody.subject());
            helper.setText(mailBody.text(), false);

            javaMailSender.send(message);
            log.info("Email sent | to: {}", mailBody.to());

        } catch (MessagingException e) {
            log.error("Error sending email | to: {} | error: {}", mailBody.to(), e.getMessage());
        }
    }

    @Async
    public void sendEmailWithAttachment(MailBody mailBody, byte[] attachment, String attachmentFilename) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(mailBody.to());
            helper.setFrom(fromEmail);
            helper.setSubject(mailBody.subject());
            helper.setText(mailBody.text(), false);

            helper.addAttachment(attachmentFilename, new ByteArrayResource(attachment));

            javaMailSender.send(message);
            log.info("Email with attachment sent | to: {} | file: {}", mailBody.to(), attachmentFilename);

        } catch (MessagingException e) {
            log.error("Error sending email with attachment | to: {} | error: {}", mailBody.to(), e.getMessage());
        }
    }

    @Async
    public void sendWelcomeEmail(String to, String name) {
        MailBody mailBody = new MailBody(
                to,
                "¡Bienvenido a GameSales!",
                "Hola " + name + ",\n\n" +
                        "Tu registro fue exitoso. ¡Bienvenido a GameSales!\n\n" +
                        "Ya puedes explorar y comprar juegos.\n\n" +
                        "Saludos,\nEl equipo de GameSales"
        );
        sendSimpleMessage(mailBody);
    }

}
