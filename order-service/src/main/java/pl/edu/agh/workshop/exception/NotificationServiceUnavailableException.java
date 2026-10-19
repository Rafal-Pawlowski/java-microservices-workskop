package pl.edu.agh.workshop.exception;

public class NotificationServiceUnavailableException extends RuntimeException{

public NotificationServiceUnavailableException(Throwable cause) {
        super("Notification service is unavailable", cause);
    }
}
