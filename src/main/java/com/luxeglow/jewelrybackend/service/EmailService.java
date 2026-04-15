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
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(order.getEmail());
            helper.setSubject("Your LuxeGlow Order #" + order.getId() + " is Confirmed");

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

            String html = """
                <div style="font-family:Arial, sans-serif; background:#f8f8f8; padding:30px;">
                    <div style="max-width:700px; margin:auto; background:#ffffff; border-radius:12px; overflow:hidden; box-shadow:0 4px 14px rgba(0,0,0,0.08);">
                        
                        <div style="background:#111111; color:#ffffff; padding:24px; text-align:center;">
                            <h1 style="margin:0; font-size:28px;">LuxeGlow Jewelry</h1>
                            <p style="margin:8px 0 0; font-size:14px; color:#dddddd;">Order Confirmation</p>
                        </div>

                        <div style="padding:30px;">
                            <p style="font-size:16px; margin:0 0 16px;">Hi %s,</p>
                            <p style="font-size:15px; color:#444;">
                                Thank you for shopping with <strong>LuxeGlow Jewelry</strong>. Your order has been placed successfully.
                            </p>

                            <div style="margin:24px 0; padding:18px; background:#fafafa; border:1px solid #eee; border-radius:10px;">
                                <p style="margin:6px 0;"><strong>Order ID:</strong> %d</p>
                                <p style="margin:6px 0;"><strong>Status:</strong> %s</p>
                                <p style="margin:6px 0;"><strong>Total Amount:</strong> ₹%.2f</p>
                            </div>

                            <h3 style="margin:24px 0 12px; color:#222;">Items Ordered</h3>
                            <table style="width:100%; border-collapse:collapse; font-size:14px;">
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

                            <p style="margin-top:24px; color:#555; font-size:14px;">
                                We will notify you once your order is shipped.
                            </p>

                            <p style="margin-top:24px; font-size:15px;">
                                Thanks,<br>
                                <strong>LuxeGlow Jewelry</strong>
                            </p>
                        </div>
                    </div>
                </div>
            """.formatted(
                    order.getCustomerName(),
                    order.getId(),
                    order.getStatus(),
                    order.getTotalAmount(),
                    itemsHtml.toString(),
                    order.getAddress().replace("\n", "<br>")
            );

            helper.setText(html, true);
            mailSender.send(message);

            System.out.println("HTML order confirmation email sent to: " + order.getEmail());

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }
}