package com.chronicare.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * AppointmentNotification entity that extends Notification.
 * Demonstrates INHERITANCE and POLYMORPHISM by providing appointment-specific
 * implementations of abstract methods from the parent class.
 */
@Entity
@DiscriminatorValue("APPOINTMENT")
public class AppointmentNotification extends Notification {
    
    // ENCAPSULATION: Private fields specific to appointment notifications
    @Column(name = "doctor_name")
    private String doctorName;
    
    @Column(name = "location")
    private String location;
    
    @Column(name = "appointment_type")
    private String appointmentType;
    
    @Column(name = "reminder_hours_before")
    private int reminderHoursBefore = 24; // Default 24 hours before
    
    @Column(name = "appointment_time")
    private LocalDateTime appointmentTime;
    
    @Column(name = "duration_minutes")
    private int durationMinutes = 30; // Default 30 minutes
    
    // Default constructor for JPA
    public AppointmentNotification() {
        super("APPOINTMENT", "NORMAL");
    }
    
    // Constructor with appointment details
    public AppointmentNotification(String doctorName, String location, 
                                 LocalDateTime appointmentTime, int reminderHoursBefore) {
        super("APPOINTMENT", "HIGH", calculateReminderTime(appointmentTime, reminderHoursBefore));
        this.doctorName = doctorName;
        this.location = location;
        this.appointmentTime = appointmentTime;
        this.reminderHoursBefore = reminderHoursBefore;
    }
    
    // Constructor with full details
    public AppointmentNotification(String doctorName, String location, String appointmentType,
                                 LocalDateTime appointmentTime, int reminderHoursBefore, 
                                 int durationMinutes) {
        super("APPOINTMENT", "HIGH", calculateReminderTime(appointmentTime, reminderHoursBefore));
        this.doctorName = doctorName;
        this.location = location;
        this.appointmentType = appointmentType;
        this.appointmentTime = appointmentTime;
        this.reminderHoursBefore = reminderHoursBefore;
        this.durationMinutes = durationMinutes;
    }
    
    // ABSTRACTION: Calculate reminder time
    private static LocalDateTime calculateReminderTime(LocalDateTime appointmentTime, int hoursBefore) {
        return appointmentTime != null ? appointmentTime.minusHours(hoursBefore) : null;
    }
    
    // POLYMORPHISM: Appointment-specific implementation of abstract method
    @Override
    public String send() {
        try {
            String method = determineDeliveryMethod();
            String message = getContent();
            
            // Simulate different sending logic based on timing and priority
            StringBuilder result = new StringBuilder();
            
            if (reminderHoursBefore <= 2) {
                result.append("SUCCESS: Urgent appointment reminder sent via SMS, email, and push notification");
                setDeliveryMethod("ALL");
                setPriority("URGENT");
            } else if (reminderHoursBefore <= 24) {
                result.append("SUCCESS: Appointment reminder sent via email and push notification");
                setDeliveryMethod("EMAIL");
            } else {
                result.append("SUCCESS: Early appointment reminder sent via email");
                setDeliveryMethod("EMAIL");
            }
            
            // Mark as sent
            markAsSent();
            
            // Add delivery details
            result.append("\nDelivery Details:");
            result.append("\n• Method: ").append(getDeliveryMethod());
            result.append("\n• Time: ").append(getFormattedSentTime());
            result.append("\n• Recipient: ").append(user != null ? user.getName() : "Unknown");
            result.append("\n• Appointment: ").append(getFormattedAppointmentTime());
            
            return result.toString();
            
        } catch (Exception e) {
            markAsFailed(e.getMessage());
            return "FAILED: Could not send appointment notification - " + e.getMessage();
        }
    }
    
    // POLYMORPHISM: Appointment-specific content generation
    @Override
    public String getContent() {
        StringBuilder content = new StringBuilder();
        
        // Add emoji and formatting based on timing
        if (reminderHoursBefore <= 2) {
            content.append("🚨 URGENT APPOINTMENT REMINDER 🚨\n\n");
        } else if (reminderHoursBefore <= 24) {
            content.append("📅 Appointment Reminder\n\n");
        } else {
            content.append("📋 Upcoming Appointment Notice\n\n");
        }
        
        content.append("You have an upcoming appointment:\n\n");
        content.append("👨‍⚕️ Doctor: Dr. ").append(doctorName).append("\n");
        content.append("🏥 Location: ").append(location).append("\n");
        
        if (appointmentTime != null) {
            content.append("🕐 Date & Time: ").append(
                appointmentTime.format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' h:mm a"))
            ).append("\n");
        }
        
        if (appointmentType != null && !appointmentType.isEmpty()) {
            content.append("🩺 Type: ").append(appointmentType).append("\n");
        }
        
        if (durationMinutes > 0) {
            content.append("⏱️ Duration: ").append(durationMinutes).append(" minutes\n");
        }
        
        // Add timing-specific instructions
        if (reminderHoursBefore <= 2) {
            content.append("\n⚠️ Your appointment is in ").append(reminderHoursBefore)
                   .append(" hour(s). Please prepare to leave soon!");
            content.append("\n🚗 Consider traffic and parking time.");
        } else if (reminderHoursBefore <= 24) {
            content.append("\n📝 Reminder: Your appointment is tomorrow.");
            content.append("\n✅ Please confirm your attendance if required.");
        } else {
            content.append("\n📅 This is an advance notice for your upcoming appointment.");
            content.append("\n📞 Contact the office if you need to reschedule.");
        }
        
        // Add preparation instructions
        content.append("\n\n📋 Preparation:");
        content.append("\n• Bring your insurance card and ID");
        content.append("\n• Arrive 15 minutes early for check-in");
        content.append("\n• Bring a list of current medications");
        
        if (appointmentType != null) {
            if (appointmentType.toLowerCase().contains("blood") || 
                appointmentType.toLowerCase().contains("lab")) {
                content.append("\n• Fasting may be required - check with your doctor");
            }
            if (appointmentType.toLowerCase().contains("physical") || 
                appointmentType.toLowerCase().contains("exam")) {
                content.append("\n• Wear comfortable, easily removable clothing");
            }
        }
        
        return content.toString();
    }
    
    // POLYMORPHISM: Appointment-specific subject line
    @Override
    public String getSubject() {
        StringBuilder subject = new StringBuilder();
        
        if (reminderHoursBefore <= 2) {
            subject.append("URGENT: ");
        } else if (reminderHoursBefore <= 24) {
            subject.append("REMINDER: ");
        } else {
            subject.append("NOTICE: ");
        }
        
        subject.append("Appointment with Dr. ").append(doctorName);
        
        if (appointmentTime != null) {
            if (reminderHoursBefore <= 24) {
                subject.append(" - ").append(
                    appointmentTime.format(java.time.format.DateTimeFormatter.ofPattern("MMM d 'at' h:mm a"))
                );
            } else {
                subject.append(" - ").append(
                    appointmentTime.format(java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy"))
                );
            }
        }
        
        return subject.toString();
    }
    
    // ABSTRACTION: Create follow-up reminder
    public AppointmentNotification createFollowUpReminder(int hoursBefore) {
        if (appointmentTime == null) {
            throw new IllegalStateException("Cannot create follow-up without appointment time");
        }
        
        AppointmentNotification followUp = new AppointmentNotification(
            doctorName, location, appointmentType, appointmentTime, hoursBefore, durationMinutes
        );
        followUp.setUser(this.user);
        
        return followUp;
    }
    
    // ABSTRACTION: Check if appointment is today
    public boolean isToday() {
        if (appointmentTime == null) return false;
        
        LocalDateTime now = LocalDateTime.now();
        return appointmentTime.toLocalDate().equals(now.toLocalDate());
    }
    
    // ABSTRACTION: Check if appointment is soon
    public boolean isSoon() {
        if (appointmentTime == null) return false;
        
        LocalDateTime now = LocalDateTime.now();
        long hoursUntilAppointment = java.time.Duration.between(now, appointmentTime).toHours();
        
        return hoursUntilAppointment <= 4 && hoursUntilAppointment >= 0;
    }
    
    // ABSTRACTION: Get time until appointment
    public String getTimeUntilAppointment() {
        if (appointmentTime == null) {
            return "Unknown";
        }
        
        LocalDateTime now = LocalDateTime.now();
        java.time.Duration duration = java.time.Duration.between(now, appointmentTime);
        
        if (duration.isNegative()) {
            return "Past appointment";
        }
        
        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;
        
        if (days > 0) {
            return days + " day(s), " + hours + " hour(s)";
        } else if (hours > 0) {
            return hours + " hour(s), " + minutes + " minute(s)";
        } else {
            return minutes + " minute(s)";
        }
    }
    
    // ABSTRACTION: Format appointment time for display
    public String getFormattedAppointmentTime() {
        return appointmentTime != null ? 
            appointmentTime.format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' h:mm a")) : 
            "Not scheduled";
    }
    
    // ENCAPSULATION: Controlled access with validation
    public void setDoctorName(String doctorName) {
        if (doctorName != null && !doctorName.trim().isEmpty()) {
            this.doctorName = doctorName.trim();
        }
    }
    
    public void setLocation(String location) {
        if (location != null && !location.trim().isEmpty()) {
            this.location = location.trim();
        }
    }
    
    public void setAppointmentType(String appointmentType) {
        this.appointmentType = appointmentType != null ? appointmentType.trim() : "";
    }
    
    public void setReminderHoursBefore(int reminderHoursBefore) {
        if (reminderHoursBefore >= 0 && reminderHoursBefore <= 168) { // Max 1 week
            this.reminderHoursBefore = reminderHoursBefore;
            // Update scheduled time if appointment time is set
            if (appointmentTime != null) {
                setScheduledTime(appointmentTime.minusHours(reminderHoursBefore));
            }
        } else {
            throw new IllegalArgumentException("Reminder hours must be between 0 and 168 (1 week)");
        }
    }
    
    public void setAppointmentTime(LocalDateTime appointmentTime) {
        this.appointmentTime = appointmentTime;
        // Update scheduled time for reminder
        if (appointmentTime != null) {
            setScheduledTime(appointmentTime.minusHours(reminderHoursBefore));
        }
    }
    
    public void setDurationMinutes(int durationMinutes) {
        if (durationMinutes > 0 && durationMinutes <= 480) { // Max 8 hours
            this.durationMinutes = durationMinutes;
        } else {
            throw new IllegalArgumentException("Duration must be between 1 and 480 minutes");
        }
    }
    
    // Getters (ENCAPSULATION: Controlled read access)
    public String getDoctorName() {
        return doctorName;
    }
    
    public String getLocation() {
        return location;
    }
    
    public String getAppointmentType() {
        return appointmentType;
    }
    
    public int getReminderHoursBefore() {
        return reminderHoursBefore;
    }
    
    public LocalDateTime getAppointmentTime() {
        return appointmentTime;
    }
    
    public int getDurationMinutes() {
        return durationMinutes;
    }
    
    @Override
    public String toString() {
        return "AppointmentNotification{" +
                "id=" + id +
                ", doctorName='" + doctorName + '\'' +
                ", location='" + location + '\'' +
                ", appointmentType='" + appointmentType + '\'' +
                ", appointmentTime=" + getFormattedAppointmentTime() +
                ", reminderHoursBefore=" + reminderHoursBefore +
                ", sent=" + sent +
                ", priority='" + priority + '\'' +
                '}';
    }
}

