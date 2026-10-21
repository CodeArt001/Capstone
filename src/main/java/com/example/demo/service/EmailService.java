package com.example.demo.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:olaniyanoluwajomiloju25@gmail.com}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String toEmail, String link) {
        String subject = "Verify Your Email";
        String fullLink = link.startsWith("http") ? link : "http://localhost:8080/auth/verify-email?token=" + link;

        String htmlContent = "<!DOCTYPE html>" +
                "<html>" +
                "<body style=\"font-family: Arial, sans-serif; background-color: #f4f4f9; padding: 20px;\">" +
                "<div style=\"max-width: 600px; margin: 0 auto; background: #ffffff; padding: 30px; border-radius: 8px;\">" +
                "<h2 style=\"color: #333333;\">Welcome aboard!</h2>" +
                "<p style=\"color: #666666; font-size: 16px;\">Click the button below to verify your email address:</p>" +
                "<div style=\"text-align: center; margin: 30px 0;\">" +
                "<a href=\"" + fullLink + "\" style=\"background-color: #007bff; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block;\">Verify Email</a>" +
                "</div>" +
                "<p style=\"color: #999999; font-size: 12px;\">If you didn't create an account, you can safely ignore this email.</p>" +
                "</div>" +
                "</body>" +
                "</html>";

        sendMail(toEmail, subject, htmlContent);
    }

    public void sendPasswordResetEmail(String toEmail, String link) {
        String subject = "Reset Your Password";
        String fullLink = link.startsWith("http") ? link : "http://localhost:8080/auth/reset-password?token=" + link;

        String htmlContent = "<!DOCTYPE html>" +
                "<html>" +
                "<body style=\"font-family: Arial, sans-serif; background-color: #f4f4f9; padding: 20px;\">" +
                "<div style=\"max-width: 600px; margin: 0 auto; background: #ffffff; padding: 30px; border-radius: 8px;\">" +
                "<h2 style=\"color: #333333;\">Password Reset Request</h2>" +
                "<p style=\"color: #666666; font-size: 16px;\">Click the button below to set a new password:</p>" +
                "<div style=\"text-align: center; margin: 30px 0;\">" +
                "<a href=\"" + fullLink + "\" style=\"background-color: #28a745; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block;\">Reset Password</a>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";

        sendMail(toEmail, subject, htmlContent);
    }

    private void sendMail(String toEmail, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException ex) {
            ex.printStackTrace();
        }
    }
}