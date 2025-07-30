package io.github.mitohondriyaa.notification.service;

import io.github.mitohondriyaa.inventory.event.InventoryRejectedEvent;
import io.github.mitohondriyaa.inventory.event.InventoryReservedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final JavaMailSender mailSender;
    private final RedisService redisService;

    @KafkaListener(topics = "inventory-reserved")
    public void orderPlaced(
        @Payload InventoryReservedEvent inventoryReservedEvent,
        @Header("messageId") String messageId
    ) {
        if (!redisService.setValue(messageId)) {
            MimeMessagePreparator messagePreparator = mimeMessage -> {
                MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);

                messageHelper.setFrom("mitohondriyaa@gmail.com");
                messageHelper.setTo(inventoryReservedEvent.getEmail().toString());
                messageHelper.setSubject(
                    String.format(
                        "Your order #%s has been placed successfully!",
                        inventoryReservedEvent.getOrderNumber()
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
                        inventoryReservedEvent.getFirstName().toString(),
                        inventoryReservedEvent.getLastName().toString(),
                        inventoryReservedEvent.getOrderNumber()
                    )
                );
            };

            mailSender.send(messagePreparator);
        }
    }

    @KafkaListener(topics = "inventory-rejected")
    public void orderCancelled(
        @Payload InventoryRejectedEvent inventoryRejectedEvent,
        @Header("messageId") String messageId
    ) {
        if (!redisService.setValue(messageId)) {
            MimeMessagePreparator messagePreparator = mimeMessage -> {
                MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);

                messageHelper.setFrom("mitohondriyaa@gmail.com");
                messageHelper.setTo(inventoryRejectedEvent.getEmail().toString());
                messageHelper.setSubject(
                    String.format(
                        "Your order #%s has been cancelled!",
                        inventoryRejectedEvent.getOrderNumber()
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
                        inventoryRejectedEvent.getFirstName().toString(),
                        inventoryRejectedEvent.getLastName().toString(),
                        inventoryRejectedEvent.getOrderNumber()
                    )
                );
            };

            mailSender.send(messagePreparator);
        }
    }
}