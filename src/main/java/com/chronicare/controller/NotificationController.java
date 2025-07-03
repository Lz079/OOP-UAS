package com.chronicare.controller;

import com.chronicare.model.*;
import com.chronicare.service.NotificationService;
import com.chronicare.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * REST Controller for Notification management in ChroniCare application.
 * Demonstrates POLYMORPHISM through different notification types.
 * Uses ABSTRACTION through service layer to hide complex business logic.
 */
@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*") // Allow CORS for frontend integration
public class NotificationController {
    
    // ABSTRACTION: Service layers hide implementation details
    private final NotificationService notificationService;
    private final UserService userService;
    
    // DEPENDENCY INJECTION: Spring automatically injects services
    @Autowired
    public NotificationController(NotificationService notificationService, UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }
    
    /**
     * Get all notifications for a specific user
     * GET /api/notifications/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>> getNotificationsByUser(@PathVariable Long userId) {
        try {
            Optional<User> user = userService.getUserById(userId);
            if (user.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            List<Notification> notifications = notificationService.getNotificationsByUser(userId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get notification by ID
     * GET /api/notifications/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Notification> getNotificationById(@PathVariable Long id) {
        try {
            Optional<Notification> notification = notificationService.getNotificationById(id);
            return notification.map(ResponseEntity::ok)
                             .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Create medication notification
     * POST /api/notifications/medication
     */
    @PostMapping("/medication")
    public ResponseEntity<MedicationNotification> createMedicationNotification(
            @RequestBody CreateMedicationNotificationRequest request) {
        try {
            Optional<User> user = userService.getUserById(request.userId);
            if (user.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            // Demonstrate POLYMORPHISM - creating specific notification type
            MedicationNotification notification = new MedicationNotification(
                request.medicationName,
                request.dosage,
                request.scheduledTime
            );
            notification.setUser(user.get());
            notification.setPriority(request.priority != null ? request.priority : "NORMAL");
            notification.setReminderHoursBefore(request.reminderHoursBefore);
            
            MedicationNotification saved = (MedicationNotification) notificationService.saveNotification(notification);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Create appointment notification
     * POST /api/notifications/appointment
     */
    @PostMapping("/appointment")
    public ResponseEntity<AppointmentNotification> createAppointmentNotification(
            @RequestBody CreateAppointmentNotificationRequest request) {
        try {
            Optional<User> user = userService.getUserById(request.userId);
            if (user.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            // Demonstrate POLYMORPHISM - creating different notification type
            AppointmentNotification notification = new AppointmentNotification(
                request.doctorName,
                request.location,
                request.appointmentType,
                request.appointmentTime,
                request.reminderHoursBefore,
                request.durationMinutes
            );
            notification.setUser(user.get());
            notification.setPriority(request.priority != null ? request.priority : "HIGH");
            
            AppointmentNotification saved = (AppointmentNotification) notificationService.saveNotification(notification);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Send notification (demonstrates POLYMORPHISM)
     * POST /api/notifications/{id}/send
     */
    @PostMapping("/{id}/send")
    public ResponseEntity<Object> sendNotification(@PathVariable Long id) {
        try {
            Optional<Notification> notificationOpt = notificationService.getNotificationById(id);
            if (notificationOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Notification notification = notificationOpt.get();
            
            // POLYMORPHISM: Different notification types implement send() differently
            String result = notification.send();
            
            // Save the updated notification
            notificationService.saveNotification(notification);
            
            // Return response with notification details
            final String sendResult = result;
            var response = new Object() {
                public final String result = sendResult;
                public final String notificationType = notification.getNotificationType();
                public final String subject = notification.getSubject();
                public final String content = notification.getContent();
                public final String priority = notification.getPriority();
                public final String deliveryMethod = notification.getDeliveryMethod();
                public final String deliveryStatus = notification.getDeliveryStatus();
                public final boolean sent = notification.isSent();
                public final String sentTime = notification.getFormattedSentTime();
            };
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get notification content (demonstrates POLYMORPHISM)
     * GET /api/notifications/{id}/content
     */
    @GetMapping("/{id}/content")
    public ResponseEntity<Object> getNotificationContent(@PathVariable Long id) {
        try {
            Optional<Notification> notificationOpt = notificationService.getNotificationById(id);
            if (notificationOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Notification notification = notificationOpt.get();
            
            // POLYMORPHISM: Different notification types generate different content
            var content = new Object() {
                public final String notificationType = notification.getNotificationType();
                public final String subject = notification.getSubject();
                public final String content = notification.getContent();
                public final String priority = notification.getPriority();
                public final boolean isHighPriority = notification.isHighPriority();
                public final boolean isUrgent = notification.isUrgent();
                public final boolean isDue = notification.isDue();
                public final boolean isOverdue = notification.isOverdue();
                public final String scheduledTime = notification.getFormattedScheduledTime();
                public final String createdDate = notification.getFormattedCreatedDate();
            };
            
            return ResponseEntity.ok(content);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get pending notifications for a user
     * GET /api/notifications/user/{userId}/pending
     */
    @GetMapping("/user/{userId}/pending")
    public ResponseEntity<List<Notification>> getPendingNotifications(@PathVariable Long userId) {
        try {
            Optional<User> user = userService.getUserById(userId);
            if (user.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            List<Notification> notifications = notificationService.getPendingNotificationsByUser(userId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get due notifications for a user
     * GET /api/notifications/user/{userId}/due
     */
    @GetMapping("/user/{userId}/due")
    public ResponseEntity<List<Notification>> getDueNotifications(@PathVariable Long userId) {
        try {
            Optional<User> user = userService.getUserById(userId);
            if (user.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            List<Notification> notifications = notificationService.getDueNotificationsByUser(userId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Send all due notifications for a user
     * POST /api/notifications/user/{userId}/send-due
     */
    @PostMapping("/user/{userId}/send-due")
    public ResponseEntity<Object> sendDueNotifications(@PathVariable Long userId) {
        try {
            Optional<User> user = userService.getUserById(userId);
            if (user.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            List<Notification> dueNotifications = notificationService.getDueNotificationsByUser(userId);
            
            // POLYMORPHISM: Send different types of notifications
            var results = dueNotifications.stream()
                .map(notification -> {
                    String sendResult = notification.send();
                    notificationService.saveNotification(notification);
                    return new Object() {
                        public final Long id = notification.getId();
                        public final String type = notification.getNotificationType();
                        public final String subject = notification.getSubject();
                        public final String result = sendResult;
                        public final boolean sent = notification.isSent();
                    };
                })
                .toList();
            
            final int processedCount = dueNotifications.size();
            final List<?> resultsList = results;
            var response = new Object() {
                public final String message = "Processed " + processedCount + " due notifications";
                public final int totalProcessed = processedCount;
                public final List<?> results = resultsList;
            };
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Delete notification
     * DELETE /api/notifications/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        try {
            if (notificationService.getNotificationById(id).isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            notificationService.deleteNotification(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Request DTOs for creating notifications
    public static class CreateMedicationNotificationRequest {
        public Long userId;
        public String medicationName;
        public String dosage;
        public LocalDateTime scheduledTime;
        public String priority;
        public int reminderHoursBefore = 0;
    }
    
    public static class CreateAppointmentNotificationRequest {
        public Long userId;
        public String doctorName;
        public String location;
        public String appointmentType;
        public LocalDateTime appointmentTime;
        public String priority;
        public int reminderHoursBefore = 24;
        public int durationMinutes = 30;
    }
}

