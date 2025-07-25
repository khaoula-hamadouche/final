package com.coderdot.services;


import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;


@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // Génère un OTP à 6 chiffres
    public String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    // Envoie un email avec l'OTP
    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("no-reply@example.com"); // Adresse fictive
        message.setTo(toEmail);
        message.setSubject("Réinitialisation de mot de passe");
        message.setText("Votre code OTP pour réinitialiser votre mot de passe est : " + otp);

        mailSender.send(message);
    }
}
