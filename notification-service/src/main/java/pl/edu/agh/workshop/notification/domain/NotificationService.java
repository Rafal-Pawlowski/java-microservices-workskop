package pl.edu.agh.workshop.notification.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    public NotificationResult send(UUID orderId, String customerEmail, String message) {
        UUID notificationId = UUID.randomUUID();
        Instant sentAt = Instant.now();

        /**
         W rzeczywistym systemie w tym miejscu moglibyśmy
         - wywołać zewnętrzny system e-mail,
         - wysłać komunikat na Kafkę,
         - zapisać powiadomienie w bazie,
         - wywołać API dostawcy SMS
         Na potrzeby warsztatu log.info() symuluje zewnętrzną integrację
         * */
        log.info("Sending notification. notificationId={}, orderId={}, recipient={}, message={}",
                notificationId,
                orderId,
                customerEmail,
                message);

        return new NotificationResult(notificationId, orderId, customerEmail, sentAt, NotificationStatus.SENT);
    }
}
