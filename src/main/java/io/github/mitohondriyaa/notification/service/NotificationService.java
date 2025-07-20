package io.github.mitohondriyaa.notification.service;

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
    public void listen(OrderPlacedEvent orderPlacedEvent) {
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
}