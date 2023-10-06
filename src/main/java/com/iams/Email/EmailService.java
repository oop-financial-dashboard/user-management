package com.iams.Email;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
@Configurable
public class EmailService {
    private JavaMailSender javaMailSender;

    @Async
    public void sendEmail(SimpleMailMessage mailMessage) {
        javaMailSender.send(mailMessage);
    }

}
