package com.luxeglow.jewelrybackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendOrderConfirmation(String toEmail, String customerName, Long orderId, Double amount) {
        System.out.println("EMAIL METHOD HIT");

        try {
            System.out.println("Trying to send email to: " + toEmail);
            System.out.println("From email: " + fromEmail);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Order Confirmation - LuxeGlow Jewelry");
            message.setText(
                    "Hi " + customerName + ",\n\n" +
                    "Your order has been successfully placed!\n\n" +
                    "Order ID: " + orderId + "\n" +
                    "Total Amount: ₹" + amount + "\n" +
                    "Status: Pending\n\n" +
                    "We will notify you once it is shipped.\n\n" +
                    "Thank you for shopping with LuxeGlow Jewelry."
            );

            mailSender.send(message);
            System.out.println("Email sent successfully");

        } catch (Exception e) {
            System.out.println("Email sending failed: " + e.getClass().getName());
            System.out.println("Reason: " + e.getMessage());
            e.printStackTrace();
        }
    }
}