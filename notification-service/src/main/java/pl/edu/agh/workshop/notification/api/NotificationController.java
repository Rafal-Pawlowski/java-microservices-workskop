package pl.edu.agh.workshop.notification.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.edu.agh.workshop.notification.domain.NotificationResult;
import pl.edu.agh.workshop.notification.domain.NotificationService;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NotificationResponse send(@Valid @RequestBody NotificationRequest request) {
        NotificationResult result = notificationService.send(request.orderId(), request.customerEmail(), request.message());
        return NotificationResponse.from(result);
    }

}
