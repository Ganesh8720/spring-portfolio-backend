package com.ganesh.portfolio_backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.ganesh.portfolio_backend.dto.ContactRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String MY_EMAIL;

    private final Map<String, String> otpCache = new HashMap<>();

    /**
     * Sends OTP Email
     */
    public void sendOtpEmail(String toEmail) {
        String otp = String.format("%04d", new Random().nextInt(10000));
        otpCache.put(toEmail, otp);

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            String htmlContent =
                "<div style='font-family: sans-serif;'>" +
                "<h2>Your OTP Code</h2>" +
                "<h1>" + otp + "</h1>" +
                "</div>";

            helper.setFrom(MY_EMAIL);
            helper.setTo(toEmail);
            helper.setSubject(otp + " is your verification code");
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Email failure: " + e.getMessage());
        }
    }

    /**
     * Verify OTP
     */
    public boolean verifyOtp(String email, String otp) {
        return otp != null && otp.equals(otpCache.get(email));
    }

    /**
     * Main Contact Flow
     */
    public void sendContactEmail(ContactRequest request) {
        sendNotificationToSelf(request);
        sendAutoReplyToUser(request.getEmail(), request.getName());
        otpCache.remove(request.getEmail());
    }

    /**
     * Send message to yourself
     */
    private void sendNotificationToSelf(ContactRequest request) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(MY_EMAIL);
        message.setTo(MY_EMAIL);
        message.setSubject("New Portfolio Message: " + request.getName());
        message.setText(
            "From: " + request.getName() + " (" + request.getEmail() + ")\n\n" +
            "Message:\n" + request.getMessage()
        );
        message.setReplyTo(request.getEmail());

        mailSender.send(message);
    }

    /**
     * Auto reply to user
     */
    private void sendAutoReplyToUser(String userEmail, String userName) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            String htmlContent =
                "<div style='font-family: Arial;'>" +
                "<h2>Thank you " + userName + "!</h2>" +
                "<p>Your message has been received.</p>" +
                "</div>";

            helper.setFrom(MY_EMAIL);
            helper.setTo(userEmail);
            helper.setSubject("Thanks for contacting Ganesh Kumar!");
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            System.err.println("Auto-reply failed: " + e.getMessage());
        }
    }
}
