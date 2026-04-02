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

    private final Map<String, String> otpCache = new HashMap<>();
    
    // Using the same email for sending and receiving
    private final String MY_EMAIL = "ganeshaicte@gmail.com";

    /**
     * Sends the Professional HTML OTP to the User
     */
    public void sendOtpEmail(String toEmail) {
        String otp = String.format("%04d", new Random().nextInt(10000));
        otpCache.put(toEmail, otp);

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            String htmlContent = 
                "<div style='font-family: sans-serif; max-width: 600px; margin: auto; border: 1px solid #e2e8f0; border-radius: 16px; overflow: hidden;'>" +
                "  <div style='background: #6366f1; padding: 25px; text-align: center;'>" +
                "    <h1 style='margin: 0; color: white; font-size: 22px;'>Verification Code</h1>" +
                "  </div>" +
                "  <div style='padding: 40px; background-color: #ffffff; color: #1f2937; line-height: 1.6;'>" +
                "    <p>Thank you for reaching out to <strong>Kondigatti Ganesh Kumar</strong>.</p>" +
                "    <p>Please use the code below to verify your email address and enable the message submit button:</p>" +
                "    <div style='text-align: center; margin: 30px 0;'>" +
                "      <span style='font-size: 40px; font-weight: 800; letter-spacing: 10px; color: #4f46e5; background: #f8fafc; padding: 15px 35px; border-radius: 10px; border: 1px solid #cbd5e1; display: inline-block;'>" + otp + "</span>" +
                "    </div>" +
                "    <p style='font-size: 13px; color: #64748b;'>If you didn't request this, you can safely ignore this email.</p>" +
                "    <hr style='border: 0; border-top: 1px solid #f1f5f9; margin: 30px 0;'>" +
                "    <p style='margin: 0; font-weight: 700; color: #4f46e5;'>Kondigatti Ganesh Kumar</p>" +
                "  </div>" +
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
     * Verify OTP Logic
     */
    public boolean verifyOtp(String email, String otp) {
        return otp != null && otp.equals(otpCache.get(email));
    }

    /**
     * Sends the final message to ganeshaicte@gmail.com
     */
    /**
     * Sends a notification to Ganesh AND an auto-reply to the sender.
     */
    public void sendContactEmail(ContactRequest request) {
        // 1. Send Notification to yourself (Ganesh)
        sendNotificationToSelf(request);

        // 2. Send Professional Auto-Reply to the User
        sendAutoReplyToUser(request.getEmail(), request.getName());

        // Cleanup OTP cache
        otpCache.remove(request.getEmail());
    }

    private void sendNotificationToSelf(ContactRequest request) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(MY_EMAIL);
        message.setTo(MY_EMAIL);
        message.setSubject("New Portfolio Message: " + request.getName());
        message.setText("From: " + request.getName() + " (" + request.getEmail() + ")\n\n" +
                      "Message Content:\n" + request.getMessage());
        message.setReplyTo(request.getEmail());
        mailSender.send(message);
    }

    private void sendAutoReplyToUser(String userEmail, String userName) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            String htmlContent = 
                "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; border: 1px solid #f0f0f0; border-radius: 10px; overflow: hidden;'>" +
                "  <div style='background-color: #4f46e5; padding: 20px; text-align: center; color: white;'>" +
                "    <h2 style='margin: 0;'>Thank You for Reaching Out!</h2>" +
                "  </div>" +
                "  <div style='padding: 30px; color: #374151; line-height: 1.7;'>" +
                "    <p>Dear <strong>" + userName + "</strong>,</p>" +
                "    <p>I have successfully received your message through my portfolio website. Thank you for your interest in connecting!</p>" +
                "    <p>I am currently reviewing your inquiry and will get back to you as soon as possible (usually within 24-48 hours).</p>" +
                "    <p>In the meantime, feel free to check out my latest projects on GitHub or connect with me on LinkedIn.</p>" +
                "    <br>" +
                "    <p style='margin-bottom: 0;'>Best Regards,</p>" +
                "    <p style='margin-top: 5px; font-weight: bold; color: #4f46e5;'>Kondigatti Ganesh Kumar</p>" +
                "    <p style='font-size: 12px; color: #9ca3af;'>Full Stack Developer | Java & Angular</p>" +
                "  </div>" +
                "  <div style='background-color: #f9fafb; padding: 15px; text-align: center; font-size: 11px; color: #9ca3af; border-top: 1px solid #eeeeee;'>" +
                "    This is an automated response. Please do not reply directly to this email." +
                "  </div>" +
                "</div>";

            helper.setFrom(MY_EMAIL);
            helper.setTo(userEmail);
            helper.setSubject("Thanks for contacting Ganesh Kumar!");
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            // Log the error but don't stop the process if auto-reply fails
            System.err.println("Failed to send auto-reply: " + e.getMessage());
        }
    }
}