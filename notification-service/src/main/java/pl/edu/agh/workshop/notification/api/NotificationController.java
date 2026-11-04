package pl.edu.agh.workshop.notification.api;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.edu.agh.workshop.notification.domain.NotificationResult;
import pl.edu.agh.workshop.notification.domain.NotificationService;

@RestController
@RequestMapping("/api/notifications")
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NotificationResponse send(@Valid @RequestBody NotificationRequest request) {
       log.info("[NOTIFICATION] HTTP notification request received: orderId={}", request.orderId());

        NotificationResult result = notificationService.send(request.orderId(), request.customerEmail(), request.message());
        return NotificationResponse.from(result);
    }
}
