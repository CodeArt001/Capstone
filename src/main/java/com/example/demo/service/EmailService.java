package com.example.demo.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailService {

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

   public void sendVerificationEmail(String toEmail, String link) {
    Email from = new Email("olaniyanoluwajomiloju25@gmail.com");
    Email to = new Email(toEmail);
    String subject = "Verify Your Email";

    // Standardized to match @GetMapping("/verify-email") in AuthController
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

    sendMail(from, subject, to, htmlContent);
}

    public void sendPasswordResetEmail(String toEmail, String link) {
    Email from = new Email("olaniyanoluwajomiloju25@gmail.com");
    Email to = new Email(toEmail);
    String subject = "Reset Your Password";

    // Standardized to match @RequestMapping("/auth") on AuthController
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

    sendMail(from, subject, to, htmlContent);
}

    private void sendMail(Email from, String subject, Email to, String htmlContent) {
        Content content = new Content("text/html", htmlContent);
        Mail mail = new Mail(from, subject, to, content);
        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            sg.api(request);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}