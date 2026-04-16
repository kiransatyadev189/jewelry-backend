package com.luxeglow.jewelrybackend.service;

import com.luxeglow.jewelrybackend.entity.Order;
import com.luxeglow.jewelrybackend.entity.OrderItem;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOrderConfirmation(Order order) {
        sendStyledEmail(
                order,
                "Your LuxeGlow Order #" + order.getId() + " is Confirmed",
                "Order Confirmation",
                "Thank you for shopping with <strong>LuxeGlow Jewelry</strong>. Your order has been placed successfully."
        );
    }

    public void sendOrderStatusUpdate(Order order) {
        String subject = "Your LuxeGlow Order #" + order.getId() + " is now " + order.getStatus();
        String heading = "Order Status Update";
        String message = buildStatusMessage(order.getStatus());

        sendStyledEmail(order, subject, heading, message);
    }

    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        System.out.println("sendPasswordResetEmail() triggered for: " + toEmail);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Reset Your LuxeGlow Password");

            String html = """
                <div style="font-family:Arial, sans-serif; background:#f8f8f8; padding:30px;">
                    <div style="max-width:600px; margin:auto; background:#ffffff; border-radius:12px; overflow:hidden; box-shadow:0 4px 14px rgba(0,0,0,0.08);">

                        <div style="background:#111111; color:#ffffff; padding:24px; text-align:center;">
                            <h1 style="margin:0;">LuxeGlow Jewelry</h1>
                            <p style="margin:8px 0 0; font-size:14px; color:#dddddd;">
                                Password Reset Request
                            </p>
                        </div>

                        <div style="padding:30px;">
                            <p style="font-size:16px;">Hello,</p>

                            <p style="font-size:15px; color:#444; line-height:1.7;">
                                We received a request to reset your password.
                            </p>

                            <p style="font-size:15px; color:#444; line-height:1.7;">
                                Click the button below to reset your password:
                            </p>

                            <div style="text-align:center; margin:30px 0;">
                                <a href="%s"
                                   style="background:#b76e79; color:white; padding:14px 24px;
                                          text-decoration:none; border-radius:8px; font-weight:600; display:inline-block;">
                                    Reset Password
                                </a>
                            </div>

                            <p style="font-size:14px; color:#777;">
                                This link will expire in 15 minutes.
                            </p>

                            <p style="font-size:14px; color:#777; word-break:break-all;">
                                If the button does not work, copy and paste this link into your browser:<br>
                                <a href="%s" style="color:#b76e79;">%s</a>
                            </p>

                            <p style="font-size:14px; color:#777;">
                                If you didn’t request this, you can safely ignore this email.
                            </p>

                            <p style="margin-top:24px;">
                                Thanks,<br>
                                <strong>LuxeGlow Jewelry</strong>
                            </p>
                        </div>
                    </div>
                </div>
            """.formatted(resetLink, resetLink, resetLink);

            helper.setText(html, true);
            mailSender.send(message);

            System.out.println("Password reset email sent to: " + toEmail);

        } catch (Exception e) {
            System.out.println("Error sending reset email: " + e.getMessage());
            throw new RuntimeException("Failed to send reset email", e);
        }
    }

    private String buildStatusMessage(String status) {
        if (status == null) {
            return "Your order status has been updated.";
        }

        return switch (status.trim().toLowerCase()) {
            case "confirmed" ->
                    "Good news! Your order has been <strong>confirmed</strong> and is now being prepared.";

            case "shipped" ->
                    "Great news! Your order has been <strong>shipped</strong> and is on the way.";

            case "out for delivery" ->
                    "Your order is <strong>out for delivery</strong> and will reach you soon. Please be available to receive it.";

            case "delivered" ->
                    "Your order has been <strong>delivered</strong>. We hope you love your purchase.";

            case "cancelled" ->
                    "Your order has been <strong>cancelled</strong>. If this was unexpected, please contact support.";

            default ->
                    "Your order status has been updated to <strong>" + status + "</strong>.";
        };
    }

    private void sendStyledEmail(Order order, String subject, String heading, String mainMessage) {
        System.out.println("sendStyledEmail() triggered for: " + order.getEmail());

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(order.getEmail());
            helper.setSubject(subject);

            StringBuilder itemsHtml = new StringBuilder();
            if (order.getItems() != null && !order.getItems().isEmpty()) {
                for (OrderItem item : order.getItems()) {
                    itemsHtml.append("""
                        <tr>
                            <td style="padding:10px; border:1px solid #eee;">%s</td>
                            <td style="padding:10px; border:1px solid #eee; text-align:center;">%d</td>
                            <td style="padding:10px; border:1px solid #eee; text-align:right;">₹%.2f</td>
                        </tr>
                    """.formatted(
                            item.getProductName(),
                            item.getQuantity(),
                            item.getPrice()
                    ));
                }
            }

            String addressHtml = order.getAddress() != null
                    ? order.getAddress().replace("\n", "<br>")
                    : "Not provided";

            String customerName = order.getCustomerName() != null ? order.getCustomerName() : "Customer";
            String orderStatus = order.getStatus() != null ? order.getStatus() : "Updated";
            double totalAmount = order.getTotalAmount() != null ? order.getTotalAmount() : 0.0;

            String html = """
                <div style="font-family:Arial, sans-serif; background:#f8f8f8; padding:30px;">
                    <div style="max-width:700px; margin:auto; background:#ffffff; border-radius:12px; overflow:hidden; box-shadow:0 4px 14px rgba(0,0,0,0.08);">

                        <div style="background:#111111; color:#ffffff; padding:24px; text-align:center;">
                            <h1 style="margin:0; font-size:28px;">LuxeGlow Jewelry</h1>
                            <p style="margin:8px 0 0; font-size:14px; color:#dddddd;">%s</p>
                        </div>

                        <div style="padding:30px;">
                            <p style="font-size:16px; margin:0 0 16px;">Hi %s,</p>
                            <p style="font-size:15px; color:#444; line-height:1.7;">
                                %s
                            </p>

                            <div style="margin:24px 0; padding:18px; background:#fafafa; border:1px solid #eee; border-radius:10px;">
                                <p style="margin:6px 0;"><strong>Order ID:</strong> %d</p>
                                <p style="margin:6px 0;"><strong>Status:</strong> %s</p>
                                <p style="margin:6px 0;"><strong>Total Amount:</strong> ₹%.2f</p>
                            </div>

                            <h3 style="margin:24px 0 12px; color:#222;">Items Ordered</h3>
                            <table style="width:100%%; border-collapse:collapse; font-size:14px;">
                                <thead>
                                    <tr style="background:#f3f3f3;">
                                        <th style="padding:10px; border:1px solid #eee; text-align:left;">Product</th>
                                        <th style="padding:10px; border:1px solid #eee; text-align:center;">Qty</th>
                                        <th style="padding:10px; border:1px solid #eee; text-align:right;">Price</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    %s
                                </tbody>
                            </table>

                            <h3 style="margin:24px 0 12px; color:#222;">Shipping Address</h3>
                            <div style="padding:16px; background:#fafafa; border:1px solid #eee; border-radius:10px; color:#444; line-height:1.6;">
                                %s
                            </div>

                            <p style="margin-top:24px; font-size:15px;">
                                Thanks,<br>
                                <strong>LuxeGlow Jewelry</strong>
                            </p>
                        </div>
                    </div>
                </div>
            """.formatted(
                    heading,
                    customerName,
                    mainMessage,
                    order.getId(),
                    orderStatus,
                    totalAmount,
                    itemsHtml.toString(),
                    addressHtml
            );

            helper.setText(html, true);
            mailSender.send(message);

            System.out.println("Email sent successfully to: " + order.getEmail());

        } catch (MessagingException e) {
            System.out.println("MessagingException while sending email: " + e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        } catch (Exception e) {
            System.out.println("General email error: " + e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }
}