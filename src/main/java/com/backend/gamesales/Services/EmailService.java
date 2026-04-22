package com.backend.gamesales.Services;

import com.backend.gamesales.Dto.Records.MailBody;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

@Service
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender javaMailSender){
        this.javaMailSender=javaMailSender;
    }
    public void sendSimpleMessage(MailBody mailBody){

        SimpleMailMessage message=new SimpleMailMessage();
        message.setTo(mailBody.to());
        message.setFrom(fromEmail);
        message.setSubject(mailBody.subject());
        message.setText(mailBody.text());
        System.out.println("FROM email: " + fromEmail);
        System.out.println("TO email: " + mailBody.to());
        javaMailSender.send(message);
    }
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
