package com.chronicare.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Abstract base class for all notifications in the ChroniCare system.
 * Demonstrates ABSTRACTION and provides foundation for POLYMORPHISM.
 * Uses JPA inheritance strategy for database persistence.
 */
@Entity
@Table(name = "notifications")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "notification_type", discriminatorType = DiscriminatorType.STRING)
public abstract class Notification {
    
    // ENCAPSULATION: Protected fields accessible to subclasses
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;
    
    @Column(name = "notification_type", insertable = false, updatable = false)
    protected String notificationType;
    
    @Column(name = "priority", nullable = false)
    protected String priority = "NORMAL";
    
    @Column(name = "created_date", nullable = false)
    protected LocalDateTime createdDate;
    
    @Column(name = "scheduled_time")
    protected LocalDateTime scheduledTime;
    
    @Column(name = "sent")
    protected boolean sent = false;
    
    @Column(name = "sent_time")
    protected LocalDateTime sentTime;
    
    @Column(name = "delivery_method")
    protected String deliveryMethod;
    
    @Column(name = "delivery_status")
    protected String deliveryStatus = "PENDING";
    
    // Many-to-One relationship with User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    protected User user;
    
    // Default constructor for JPA
    public Notification() {
        this.createdDate = LocalDateTime.now();
    }
    
    // Constructor with basic fields
    public Notification(String notificationType, String priority) {
        this();
        this.notificationType = notificationType;
        setPriority(priority);
    }
    
    // Constructor with scheduling
    public Notification(String notificationType, String priority, LocalDateTime scheduledTime) {
        this(notificationType, priority);
        this.scheduledTime = scheduledTime;
    }
    
    // ABSTRACTION: Abstract methods that subclasses must implement (POLYMORPHISM)
    public abstract String send();
    public abstract String getContent();
    public abstract String getSubject();
    
    // ENCAPSULATION: Controlled access with validation
    public void setPriority(String priority) {
        if (priority != null) {
            String upperPriority = priority.toUpperCase();
            if (upperPriority.equals("LOW") || upperPriority.equals("NORMAL") || 
                upperPriority.equals("HIGH") || upperPriority.equals("URGENT")) {
                this.priority = upperPriority;
            } else {
                throw new IllegalArgumentException("Priority must be LOW, NORMAL, HIGH, or URGENT");
            }
        }
    }
    
    public void setDeliveryMethod(String deliveryMethod) {
        if (deliveryMethod != null) {
            String upperMethod = deliveryMethod.toUpperCase();
            if (upperMethod.equals("EMAIL") || upperMethod.equals("SMS") || 
                upperMethod.equals("PUSH") || upperMethod.equals("ALL")) {
                this.deliveryMethod = upperMethod;
            } else {
                throw new IllegalArgumentException("Delivery method must be EMAIL, SMS, PUSH, or ALL");
            }
        }
    }
    
    public void setDeliveryStatus(String deliveryStatus) {
        if (deliveryStatus != null) {
            String upperStatus = deliveryStatus.toUpperCase();
            if (upperStatus.equals("PENDING") || upperStatus.equals("SENT") || 
                upperStatus.equals("DELIVERED") || upperStatus.equals("FAILED")) {
                this.deliveryStatus = upperStatus;
            } else {
                throw new IllegalArgumentException("Delivery status must be PENDING, SENT, DELIVERED, or FAILED");
            }
        }
    }
    
    // ABSTRACTION: Simple interface for priority checking
    public boolean isHighPriority() {
        return "HIGH".equals(priority) || "URGENT".equals(priority);
    }
    
    public boolean isUrgent() {
        return "URGENT".equals(priority);
    }
    
    // ABSTRACTION: Check if notification is due
    public boolean isDue() {
        if (scheduledTime == null) {
            return true; // Send immediately if no schedule
        }
        return LocalDateTime.now().isAfter(scheduledTime) || LocalDateTime.now().isEqual(scheduledTime);
    }
    
    // ABSTRACTION: Check if notification is overdue
    public boolean isOverdue() {
        if (sent || scheduledTime == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(scheduledTime.plusMinutes(15)); // 15 minutes grace period
    }
    
    // ABSTRACTION: Determine delivery method based on priority
    protected String determineDeliveryMethod() {
        if (deliveryMethod != null) {
            return deliveryMethod;
        }
        
        // Auto-determine based on priority
        switch (priority) {
            case "URGENT":
                return "ALL"; // SMS, Email, and Push
            case "HIGH":
                return "SMS"; // SMS and Push
            case "NORMAL":
                return "PUSH"; // Push notification only
            case "LOW":
                return "EMAIL"; // Email only
            default:
                return "PUSH";
        }
    }
    
    // ABSTRACTION: Mark notification as sent
    public void markAsSent() {
        this.sent = true;
        this.sentTime = LocalDateTime.now();
        this.deliveryStatus = "SENT";
    }
    
    public void markAsDelivered() {
        this.deliveryStatus = "DELIVERED";
    }
    
    public void markAsFailed(String reason) {
        this.deliveryStatus = "FAILED";
        // Could store failure reason in a separate field if needed
    }
    
    // ABSTRACTION: Calculate age of notification
    public long getAgeInMinutes() {
        return java.time.Duration.between(createdDate, LocalDateTime.now()).toMinutes();
    }
    
    public long getAgeInHours() {
        return java.time.Duration.between(createdDate, LocalDateTime.now()).toHours();
    }
    
    // ABSTRACTION: Format times for display
    public String getFormattedCreatedDate() {
        return createdDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
    
    public String getFormattedScheduledTime() {
        return scheduledTime != null ? 
            scheduledTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : 
            "Not scheduled";
    }
    
    public String getFormattedSentTime() {
        return sentTime != null ? 
            sentTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : 
            "Not sent";
    }
    
    // Getters (ENCAPSULATION: Controlled read access)
    public Long getId() {
        return id;
    }
    
    public String getNotificationType() {
        return notificationType;
    }
    
    public String getPriority() {
        return priority;
    }
    
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
    
    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }
    
    public boolean isSent() {
        return sent;
    }
    
    public LocalDateTime getSentTime() {
        return sentTime;
    }
    
    public String getDeliveryMethod() {
        return deliveryMethod;
    }
    
    public String getDeliveryStatus() {
        return deliveryStatus;
    }
    
    public User getUser() {
        return user;
    }
    
    // Setters for JPA
    public void setId(Long id) {
        this.id = id;
    }
    
    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }
    
    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
    
    public void setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }
    
    public void setSent(boolean sent) {
        this.sent = sent;
    }
    
    public void setSentTime(LocalDateTime sentTime) {
        this.sentTime = sentTime;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Notification that = (Notification) obj;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
    
    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", type='" + notificationType + '\'' +
                ", priority='" + priority + '\'' +
                ", sent=" + sent +
                ", deliveryStatus='" + deliveryStatus + '\'' +
                ", scheduledTime=" + getFormattedScheduledTime() +
                '}';
    }
}

