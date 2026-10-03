package com.example.tasteebackend.services.impl;

import com.example.tasteebackend.model.Order;
import com.example.tasteebackend.services.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendOrderReceipt(Order order) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(order.getCustomer().getEmail());
            helper.setSubject("Tastee Order Receipt #" + order.getId());

            String body = """
                    Thank you for ordering from Tastee!
                    
                    Order ID: %s
                    
                    Restaurant: %s
                    
                    Delivery Address:
                    %s
                    
                    Total:
                    $%.2f
                    
                    Status:
                    PAID
                    
                    Your food is being prepared!
                    """.formatted(
                    order.getId(),
                    order.getRestaurant().getName(),
                    order.getDeliveryAddress(),
                    order.getTotal()
            );

            helper.setText(body);
            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed sending email", e);
        }
    }
}