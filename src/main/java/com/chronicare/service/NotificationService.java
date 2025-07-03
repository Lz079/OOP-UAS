package com.chronicare.service;

import com.chronicare.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Service class for Notification management in ChroniCare application.
 * Demonstrates ABSTRACTION and POLYMORPHISM through different notification types.
 * Uses in-memory storage for demonstration (can be replaced with JPA repository).
 */
@Service
public class NotificationService {
    
    // ENCAPSULATION: Private data storage
    private final Map<Long, Notification> notifications = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    // DEPENDENCY INJECTION: Other services for related operations
    private final UserService userService;
    private final MedicationService medicationService;
    
    @Autowired
    public NotificationService(UserService userService, MedicationService medicationService) {
        this.userService = userService;
        this.medicationService = medicationService;
        initializeSampleData();
    }
    
    // ABSTRACTION: Hide complex initialization logic
    private void initializeSampleData() {
        // Create sample notifications for demonstration
        Optional<User> user1 = userService.getUserById(1L);
        if (user1.isPresent()) {
            // Medication notification for Metformin
            MedicationNotification medNotif1 = new MedicationNotification(
                "Metformin", "500mg", LocalDateTime.now().plusHours(1)
            );
            medNotif1.setId(idGenerator.getAndIncrement());
            medNotif1.setUser(user1.get());
            medNotif1.setPriority("NORMAL");
            notifications.put(medNotif1.getId(), medNotif1);
            
            // Urgent medication notification for Lisinopril
            MedicationNotification medNotif2 = new MedicationNotification(
                "Lisinopril", "10mg", LocalDateTime.now().plusMinutes(30)
            );
            medNotif2.setId(idGenerator.getAndIncrement());
            medNotif2.setUser(user1.get());
            medNotif2.setPriority("HIGH");
            notifications.put(medNotif2.getId(), medNotif2);
            
            // Appointment notification
            AppointmentNotification apptNotif1 = new AppointmentNotification(
                "Smith", "Main Street Medical Center", "Regular Checkup",
                LocalDateTime.now().plusDays(1), 24, 30
            );
            apptNotif1.setId(idGenerator.getAndIncrement());
            apptNotif1.setUser(user1.get());
            notifications.put(apptNotif1.getId(), apptNotif1);
        }
        
        Optional<User> user2 = userService.getUserById(2L);
        if (user2.isPresent()) {
            // Medication notification for Albuterol
            MedicationNotification medNotif3 = new MedicationNotification(
                "Albuterol", "90mcg", LocalDateTime.now().plusHours(2)
            );
            medNotif3.setId(idGenerator.getAndIncrement());
            medNotif3.setUser(user2.get());
            medNotif3.setPriority("HIGH");
            notifications.put(medNotif3.getId(), medNotif3);
            
            // Appointment notification for specialist
            AppointmentNotification apptNotif2 = new AppointmentNotification(
                "Johnson", "Pulmonology Clinic", "Asthma Follow-up",
                LocalDateTime.now().plusDays(3), 48, 45
            );
            apptNotif2.setId(idGenerator.getAndIncrement());
            apptNotif2.setUser(user2.get());
            notifications.put(apptNotif2.getId(), apptNotif2);
        }
        
        System.out.println("Initialized " + notifications.size() + " sample notifications");
    }
    
    /**
     * Get all notifications
     * ABSTRACTION: Simple interface for complex data retrieval
     */
    public List<Notification> getAllNotifications() {
        return new ArrayList<>(notifications.values());
    }
    
    /**
     * Get notification by ID
     * ABSTRACTION: Hide lookup complexity
     */
    public Optional<Notification> getNotificationById(Long id) {
        return Optional.ofNullable(notifications.get(id));
    }
    
    /**
     * Get notifications by user ID
     * ABSTRACTION: Hide filtering logic
     */
    public List<Notification> getNotificationsByUser(Long userId) {
        return notifications.values().stream()
            .filter(notification -> notification.getUser() != null && 
                   notification.getUser().getId().equals(userId))
            .sorted(Comparator.comparing(Notification::getCreatedDate).reversed())
            .collect(Collectors.toList());
    }
    
    /**
     * Save notification (create or update)
     * ABSTRACTION: Hide persistence logic
     * POLYMORPHISM: Works with any Notification subtype
     */
    public Notification saveNotification(Notification notification) {
        if (notification == null) {
            throw new IllegalArgumentException("Notification cannot be null");
        }
        
        // Validate notification data (ENCAPSULATION in action)
        validateNotification(notification);
        
        // Assign ID if new notification
        if (notification.getId() == null) {
            notification.setId(idGenerator.getAndIncrement());
        }
        
        // Set creation date if new
        if (notification.getCreatedDate() == null) {
            notification.setCreatedDate(LocalDateTime.now());
        }
        
        notifications.put(notification.getId(), notification);
        
        // POLYMORPHISM: Different notification types have different string representations
        System.out.println("Notification saved: " + notification.getNotificationType() + 
                         " (ID: " + notification.getId() + ") - " + notification.getSubject());
        
        return notification;
    }
    
    /**
     * Delete notification by ID
     * ABSTRACTION: Hide deletion complexity
     */
    public void deleteNotification(Long id) {
        Notification removedNotification = notifications.remove(id);
        if (removedNotification != null) {
            System.out.println("Notification deleted: " + removedNotification.getNotificationType() + 
                             " (ID: " + id + ")");
        }
    }
    
    /**
     * Get pending notifications for a user
     * ABSTRACTION: Hide filtering logic
     */
    public List<Notification> getPendingNotificationsByUser(Long userId) {
        return getNotificationsByUser(userId).stream()
            .filter(notification -> !notification.isSent())
            .collect(Collectors.toList());
    }
    
    /**
     * Get due notifications for a user
     * ABSTRACTION: Hide complex timing calculations
     */
    public List<Notification> getDueNotificationsByUser(Long userId) {
        return getNotificationsByUser(userId).stream()
            .filter(notification -> !notification.isSent())
            .filter(Notification::isDue)
            .collect(Collectors.toList());
    }
    
    /**
     * Get overdue notifications for a user
     * ABSTRACTION: Hide complex timing calculations
     */
    public List<Notification> getOverdueNotificationsByUser(Long userId) {
        return getNotificationsByUser(userId).stream()
            .filter(notification -> !notification.isSent())
            .filter(Notification::isOverdue)
            .collect(Collectors.toList());
    }
    
    /**
     * Send all due notifications for a user
     * ABSTRACTION: Hide complex sending logic
     * POLYMORPHISM: Different notification types implement send() differently
     */
    public List<Object> sendDueNotificationsForUser(Long userId) {
        List<Notification> dueNotifications = getDueNotificationsByUser(userId);
        
        return dueNotifications.stream()
            .map(notification -> {
                // POLYMORPHISM: Each notification type implements send() differently
                String sendResult = notification.send();
                saveNotification(notification);
                
                return new Object() {
                    public final Long id = notification.getId();
                    public final String type = notification.getNotificationType();
                    public final String subject = notification.getSubject();
                    public final String result = sendResult;
                    public final boolean sent = notification.isSent();
                    public final String sentTime = notification.getFormattedSentTime();
                };
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Create medication notification
     * ABSTRACTION: Hide notification creation complexity
     */
    public MedicationNotification createMedicationNotification(Long userId, String medicationName, 
                                                             String dosage, LocalDateTime scheduledTime, 
                                                             String priority) {
        Optional<User> user = userService.getUserById(userId);
        if (user.isEmpty()) {
            throw new IllegalArgumentException("User not found with ID: " + userId);
        }
        
        MedicationNotification notification = new MedicationNotification(
            medicationName, dosage, scheduledTime
        );
        notification.setUser(user.get());
        notification.setPriority(priority != null ? priority : "NORMAL");
        
        return (MedicationNotification) saveNotification(notification);
    }
    
    /**
     * Create appointment notification
     * ABSTRACTION: Hide notification creation complexity
     */
    public AppointmentNotification createAppointmentNotification(Long userId, String doctorName, 
                                                               String location, String appointmentType,
                                                               LocalDateTime appointmentTime, 
                                                               int reminderHoursBefore) {
        Optional<User> user = userService.getUserById(userId);
        if (user.isEmpty()) {
            throw new IllegalArgumentException("User not found with ID: " + userId);
        }
        
        AppointmentNotification notification = new AppointmentNotification(
            doctorName, location, appointmentType, appointmentTime, reminderHoursBefore, 30
        );
        notification.setUser(user.get());
        
        return (AppointmentNotification) saveNotification(notification);
    }
    
    /**
     * Get notification statistics
     * ABSTRACTION: Hide complex statistical calculations
     * POLYMORPHISM: Statistics work with all notification types
     */
    public Object getNotificationStatistics() {
        List<Notification> allNotifications = getAllNotifications();
        
        // Calculate statistics using POLYMORPHISM
        Map<String, Long> typeCounts = allNotifications.stream()
            .collect(Collectors.groupingBy(
                Notification::getNotificationType,
                Collectors.counting()
            ));
        
        Map<String, Long> priorityCounts = allNotifications.stream()
            .collect(Collectors.groupingBy(
                Notification::getPriority,
                Collectors.counting()
            ));
        
        Map<String, Long> statusCounts = allNotifications.stream()
            .collect(Collectors.groupingBy(
                Notification::getDeliveryStatus,
                Collectors.counting()
            ));
        
        long sentCount = allNotifications.stream()
            .mapToLong(notification -> notification.isSent() ? 1 : 0)
            .sum();
        
        long dueCount = allNotifications.stream()
            .mapToLong(notification -> notification.isDue() && !notification.isSent() ? 1 : 0)
            .sum();
        
        long overdueCount = allNotifications.stream()
            .mapToLong(notification -> notification.isOverdue() && !notification.isSent() ? 1 : 0)
            .sum();
        
        return new Object() {
            public final int totalNotifications = allNotifications.size();
            public final long sentNotifications = sentCount;
            public final long pendingNotifications = allNotifications.size() - sentCount;
            public final long dueNotifications = dueCount;
            public final long overdueNotifications = overdueCount;
            public final Map<String, Long> typeDistribution = typeCounts;
            public final Map<String, Long> priorityDistribution = priorityCounts;
            public final Map<String, Long> statusDistribution = statusCounts;
            public final double sentPercentage = allNotifications.isEmpty() ? 0.0 : 
                (double) sentCount / allNotifications.size() * 100;
        };
    }
    
    /**
     * Get notifications by type
     * ABSTRACTION: Hide filtering logic
     * POLYMORPHISM: Filter by notification type
     */
    public List<Notification> getNotificationsByType(String type) {
        return notifications.values().stream()
            .filter(notification -> notification.getNotificationType().equalsIgnoreCase(type))
            .collect(Collectors.toList());
    }
    
    /**
     * Get notifications by priority
     * ABSTRACTION: Hide filtering logic
     */
    public List<Notification> getNotificationsByPriority(String priority) {
        return notifications.values().stream()
            .filter(notification -> notification.getPriority().equalsIgnoreCase(priority))
            .collect(Collectors.toList());
    }
    
    /**
     * Get high priority notifications
     * ABSTRACTION: Hide priority filtering logic
     */
    public List<Notification> getHighPriorityNotifications() {
        return notifications.values().stream()
            .filter(Notification::isHighPriority)
            .collect(Collectors.toList());
    }
    
    /**
     * Get urgent notifications
     * ABSTRACTION: Hide urgency filtering logic
     */
    public List<Notification> getUrgentNotifications() {
        return notifications.values().stream()
            .filter(Notification::isUrgent)
            .collect(Collectors.toList());
    }
    
    /**
     * Schedule automatic medication notifications for a user
     * ABSTRACTION: Hide complex scheduling logic
     */
    public List<MedicationNotification> scheduleAutomaticMedicationNotifications(Long userId) {
        List<Medication> userMedications = medicationService.getMedicationsByUser(userId);
        List<MedicationNotification> scheduledNotifications = new ArrayList<>();
        
        for (Medication medication : userMedications) {
            if (medication.isActive()) {
                List<String> scheduleTimes = medication.getScheduleTimes();
                
                for (String time : scheduleTimes) {
                    // Create notification for tomorrow at the scheduled time
                    LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
                    String[] timeParts = time.split(":");
                    LocalDateTime scheduledTime = tomorrow
                        .withHour(Integer.parseInt(timeParts[0]))
                        .withMinute(Integer.parseInt(timeParts[1]))
                        .withSecond(0);
                    
                    MedicationNotification notification = createMedicationNotification(
                        userId, medication.getName(), medication.getDosage(), 
                        scheduledTime, "NORMAL"
                    );
                    
                    scheduledNotifications.add(notification);
                }
            }
        }
        
        System.out.println("Scheduled " + scheduledNotifications.size() + 
                         " automatic medication notifications for user " + userId);
        
        return scheduledNotifications;
    }
    
    /**
     * Check if notification exists
     * ABSTRACTION: Simple existence check
     */
    public boolean notificationExists(Long id) {
        return notifications.containsKey(id);
    }
    
    /**
     * Get notification count
     * ABSTRACTION: Simple count operation
     */
    public long getNotificationCount() {
        return notifications.size();
    }
    
    /**
     * Validate notification data
     * ENCAPSULATION: Private validation logic
     * POLYMORPHISM: Validation works with all notification types
     */
    private void validateNotification(Notification notification) {
        if (notification.getUser() == null) {
            throw new IllegalArgumentException("Notification must be associated with a user");
        }
        
        if (notification.getPriority() == null || notification.getPriority().trim().isEmpty()) {
            throw new IllegalArgumentException("Notification priority cannot be null or empty");
        }
        
        // Type-specific validation using POLYMORPHISM
        if (notification instanceof MedicationNotification) {
            MedicationNotification medNotif = (MedicationNotification) notification;
            if (medNotif.getMedicationName() == null || medNotif.getMedicationName().trim().isEmpty()) {
                throw new IllegalArgumentException("Medication notification must have a medication name");
            }
            if (medNotif.getDosage() == null || medNotif.getDosage().trim().isEmpty()) {
                throw new IllegalArgumentException("Medication notification must have a dosage");
            }
        } else if (notification instanceof AppointmentNotification) {
            AppointmentNotification apptNotif = (AppointmentNotification) notification;
            if (apptNotif.getDoctorName() == null || apptNotif.getDoctorName().trim().isEmpty()) {
                throw new IllegalArgumentException("Appointment notification must have a doctor name");
            }
            if (apptNotif.getLocation() == null || apptNotif.getLocation().trim().isEmpty()) {
                throw new IllegalArgumentException("Appointment notification must have a location");
            }
            if (apptNotif.getAppointmentTime() == null) {
                throw new IllegalArgumentException("Appointment notification must have an appointment time");
            }
        }
    }
}

