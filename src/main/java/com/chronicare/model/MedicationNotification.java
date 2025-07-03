package com.chronicare.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * MedicationNotification entity that extends Notification.
 * Demonstrates INHERITANCE and POLYMORPHISM by providing medication-specific
 * implementations of abstract methods from the parent class.
 */
@Entity
@DiscriminatorValue("MEDICATION")
public class MedicationNotification extends Notification {
    
    // ENCAPSULATION: Private fields specific to medication notifications
    @Column(name = "medication_name")
    private String medicationName;
    
    @Column(name = "dosage")
    private String dosage;
    
    @Column(name = "reminder_hours_before")
    private int reminderHoursBefore = 0; // 0 means at scheduled time
    
    // Many-to-One relationship with Medication
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medication_id")
    private Medication medication;
    
    // Default constructor for JPA
    public MedicationNotification() {
        super("MEDICATION", "NORMAL");
    }
    
    // Constructor with medication details
    public MedicationNotification(String medicationName, String dosage, LocalDateTime scheduledTime) {
        super("MEDICATION", "NORMAL", scheduledTime);
        this.medicationName = medicationName;
        this.dosage = dosage;
    }
    
    // Constructor with medication reference
    public MedicationNotification(Medication medication, LocalDateTime scheduledTime) {
        super("MEDICATION", "NORMAL", scheduledTime);
        this.medication = medication;
        this.medicationName = medication.getName();
        this.dosage = medication.getDosage();
    }
    
    // Constructor with priority and reminder timing
    public MedicationNotification(Medication medication, LocalDateTime scheduledTime, 
                                String priority, int reminderHoursBefore) {
        super("MEDICATION", priority, scheduledTime);
        this.medication = medication;
        this.medicationName = medication.getName();
        this.dosage = medication.getDosage();
        this.reminderHoursBefore = reminderHoursBefore;
    }
    
    // POLYMORPHISM: Medication-specific implementation of abstract method
    @Override
    public String send() {
        try {
            String method = determineDeliveryMethod();
            String message = getContent();
            
            // Simulate different sending logic based on priority and method
            StringBuilder result = new StringBuilder();
            
            if (isUrgent()) {
                result.append("SUCCESS: Urgent medication notification sent via SMS, email, and push notification");
                setDeliveryMethod("ALL");
            } else if (isHighPriority()) {
                result.append("SUCCESS: High priority medication notification sent via SMS and push notification");
                setDeliveryMethod("SMS");
            } else {
                result.append("SUCCESS: Medication notification sent via push notification");
                setDeliveryMethod("PUSH");
            }
            
            // Mark as sent
            markAsSent();
            
            // Add delivery details
            result.append("\nDelivery Details:");
            result.append("\n• Method: ").append(getDeliveryMethod());
            result.append("\n• Time: ").append(getFormattedSentTime());
            result.append("\n• Recipient: ").append(user != null ? user.getName() : "Unknown");
            
            return result.toString();
            
        } catch (Exception e) {
            markAsFailed(e.getMessage());
            return "FAILED: Could not send medication notification - " + e.getMessage();
        }
    }
    
    // POLYMORPHISM: Medication-specific content generation
    @Override
    public String getContent() {
        StringBuilder content = new StringBuilder();
        
        // Add emoji and formatting based on priority
        if (isUrgent()) {
            content.append("🚨 URGENT MEDICATION REMINDER 🚨\n\n");
        } else if (isHighPriority()) {
            content.append("⚠️ IMPORTANT MEDICATION REMINDER ⚠️\n\n");
        } else {
            content.append("⏰ Medication Reminder\n\n");
        }
        
        content.append("It's time to take your medication:\n\n");
        content.append("💊 Medication: ").append(medicationName).append("\n");
        content.append("📏 Dosage: ").append(dosage).append("\n");
        
        if (scheduledTime != null) {
            content.append("🕐 Scheduled Time: ").append(
                scheduledTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
            ).append("\n");
        }
        
        // Add medication-specific instructions if available
        if (medication != null && medication.getInstructions() != null && 
            !medication.getInstructions().isEmpty()) {
            content.append("📋 Instructions: ").append(medication.getInstructions()).append("\n");
        }
        
        // Add adherence motivation
        if (medication != null) {
            double adherence = medication.getAdherenceRate();
            if (adherence < 0.7) {
                content.append("\n🎯 Your current adherence is ").append(medication.getAdherencePercentage())
                       .append(". Taking this medication will help improve your health outcomes!");
            } else if (adherence >= 0.9) {
                content.append("\n🌟 Great job! You're maintaining excellent medication adherence at ")
                       .append(medication.getAdherencePercentage()).append("!");
            }
        }
        
        content.append("\n\nTap here to mark as taken or provide a reason if missed.");
        
        return content.toString();
    }
    
    // POLYMORPHISM: Medication-specific subject line
    @Override
    public String getSubject() {
        StringBuilder subject = new StringBuilder();
        
        if (isUrgent()) {
            subject.append("URGENT: ");
        } else if (isHighPriority()) {
            subject.append("IMPORTANT: ");
        }
        
        subject.append("Time to take ").append(medicationName);
        
        if (reminderHoursBefore > 0) {
            subject.append(" (").append(reminderHoursBefore).append("h reminder)");
        }
        
        return subject.toString();
    }
    
    // ABSTRACTION: Create reminder notification
    public MedicationNotification createReminderNotification(int hoursBefore) {
        if (scheduledTime == null) {
            throw new IllegalStateException("Cannot create reminder without scheduled time");
        }
        
        LocalDateTime reminderTime = scheduledTime.minusHours(hoursBefore);
        MedicationNotification reminder = new MedicationNotification(
            medication, reminderTime, "HIGH", hoursBefore
        );
        reminder.setUser(this.user);
        
        return reminder;
    }
    
    // ABSTRACTION: Check if this is a reminder notification
    public boolean isReminderNotification() {
        return reminderHoursBefore > 0;
    }
    
    // ABSTRACTION: Get notification timing description
    public String getTimingDescription() {
        if (reminderHoursBefore == 0) {
            return "Medication time notification";
        } else if (reminderHoursBefore == 1) {
            return "1 hour reminder";
        } else {
            return reminderHoursBefore + " hour reminder";
        }
    }
    
    // ENCAPSULATION: Controlled access with validation
    public void setMedicationName(String medicationName) {
        if (medicationName != null && !medicationName.trim().isEmpty()) {
            this.medicationName = medicationName.trim();
        }
    }
    
    public void setDosage(String dosage) {
        if (dosage != null && !dosage.trim().isEmpty()) {
            this.dosage = dosage.trim();
        }
    }
    
    public void setReminderHoursBefore(int reminderHoursBefore) {
        if (reminderHoursBefore >= 0 && reminderHoursBefore <= 24) {
            this.reminderHoursBefore = reminderHoursBefore;
        } else {
            throw new IllegalArgumentException("Reminder hours must be between 0 and 24");
        }
    }
    
    // Getters (ENCAPSULATION: Controlled read access)
    public String getMedicationName() {
        return medicationName;
    }
    
    public String getDosage() {
        return dosage;
    }
    
    public int getReminderHoursBefore() {
        return reminderHoursBefore;
    }
    
    public Medication getMedication() {
        return medication;
    }
    
    // Setters for JPA
    public void setMedication(Medication medication) {
        this.medication = medication;
        if (medication != null) {
            this.medicationName = medication.getName();
            this.dosage = medication.getDosage();
        }
    }
    
    @Override
    public String toString() {
        return "MedicationNotification{" +
                "id=" + id +
                ", medicationName='" + medicationName + '\'' +
                ", dosage='" + dosage + '\'' +
                ", priority='" + priority + '\'' +
                ", sent=" + sent +
                ", scheduledTime=" + getFormattedScheduledTime() +
                ", timingDescription='" + getTimingDescription() + '\'' +
                '}';
    }
}

