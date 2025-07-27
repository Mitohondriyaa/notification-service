package io.github.mitohondriyaa.notification.service;

import io.github.mitohondriyaa.order.event.OrderCancelledEvent;
import io.github.mitohondriyaa.order.event.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final JavaMailSender mailSender;

    @KafkaListener(topics = "order-placed")
    public void orderPlaced(OrderPlacedEvent orderPlacedEvent) {
        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);

            messageHelper.setFrom("mitohondriyaa@gmail.com");
            messageHelper.setTo(orderPlacedEvent.getEmail().toString());
            messageHelper.setSubject(
                String.format(
                    "Your order #%s has been placed successfully!",
                    orderPlacedEvent.getOrderNumber()
                )
            );
            messageHelper.setText(
                String.format(
                    """
                        Hi, %s %s,
                        
                        Thank you for your order!
                        
                        Your order #%s has been placed successfully!
                        
                        Best regards,
                        Mitohondriyaa
                        """,
                    orderPlacedEvent.getFirstName().toString(),
                    orderPlacedEvent.getLastName().toString(),
                    orderPlacedEvent.getOrderNumber()
                )
            );
        };

        mailSender.send(messagePreparator);
    }

    @KafkaListener(topics = "order-cancelled")
    public void orderCancelled(OrderCancelledEvent orderCancelledEvent) {
        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);

            messageHelper.setFrom("mitohondriyaa@gmail.com");
            messageHelper.setTo(orderCancelledEvent.getEmail().toString());
            messageHelper.setSubject(
                String.format(
                    "Your order #%s has been cancelled!",
                    orderCancelledEvent.getOrderNumber()
                )
            );
            messageHelper.setText(
                String.format(
                    """
                        Hi, %s %s,
                        
                        Your order #%s has been cancelled!
                        
                        Best regards,
                        Mitohondriyaa
                        """,
                    orderCancelledEvent.getFirstName().toString(),
                    orderCancelledEvent.getLastName().toString(),
                    orderCancelledEvent.getOrderNumber()
                )
            );
        };

        mailSender.send(messagePreparator);
    }
}