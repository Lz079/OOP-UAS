package com.chronicare.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * MedicationRecord entity that extends HealthRecord.
 * Demonstrates INHERITANCE by extending the base HealthRecord class.
 * Specialized for tracking medication adherence and timing.
 */
@Entity
@DiscriminatorValue("MEDICATION")
public class MedicationRecord extends HealthRecord {
    
    // ENCAPSULATION: Private fields specific to medication records
    @Column(name = "medication_name")
    private String medicationName;
    
    @Column(name = "dosage")
    private String dosage;
    
    @Column(name = "time_scheduled")
    private LocalDateTime timeScheduled;
    
    @Column(name = "time_taken")
    private LocalDateTime timeTaken;
    
    @Column(name = "taken")
    private boolean taken = false;
    
    @Column(name = "missed_reason")
    private String missedReason;
    
    // Many-to-One relationship with Medication
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medication_id")
    private Medication medication;
    
    // Default constructor for JPA
    public MedicationRecord() {
        super("MEDICATION");
    }
    
    // Constructor with medication details
    public MedicationRecord(String medicationName, String dosage, LocalDateTime timeScheduled) {
        super("MEDICATION");
        this.medicationName = medicationName;
        this.dosage = dosage;
        this.timeScheduled = timeScheduled;
    }
    
    // Constructor with medication reference
    public MedicationRecord(Medication medication, LocalDateTime timeScheduled) {
        super("MEDICATION");
        this.medication = medication;
        this.medicationName = medication.getName();
        this.dosage = medication.getDosage();
        this.timeScheduled = timeScheduled;
    }
    
    // INHERITANCE: Override parent method with specialized behavior
    @Override
    public String getSummary() {
        String status = taken ? "Taken" : "Missed";
        String timeInfo = taken ? 
            (timeTaken != null ? " at " + timeTaken.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")) : "") :
            (timeScheduled != null ? " (scheduled for " + timeScheduled.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")) + ")" : "");
        
        return String.format("Medication: %s (%s) - %s%s", 
            medicationName, dosage, status, timeInfo);
    }
    
    // INHERITANCE: Override parent method with medication-specific details
    @Override
    public String getDetails() {
        StringBuilder details = new StringBuilder();
        details.append("Medication Record Details:\n");
        details.append("• Medication: ").append(medicationName).append("\n");
        details.append("• Dosage: ").append(dosage).append("\n");
        details.append("• Scheduled Time: ").append(
            timeScheduled != null ? timeScheduled.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "Not scheduled"
        ).append("\n");
        details.append("• Status: ").append(taken ? "Taken" : "Missed").append("\n");
        
        if (taken && timeTaken != null) {
            details.append("• Taken Time: ").append(timeTaken.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append("\n");
            details.append("• Timing: ").append(getTimingStatus()).append("\n");
        }
        
        if (!taken && missedReason != null && !missedReason.isEmpty()) {
            details.append("• Missed Reason: ").append(missedReason).append("\n");
        }
        
        if (notes != null && !notes.isEmpty()) {
            details.append("• Notes: ").append(notes).append("\n");
        }
        
        return details.toString();
    }
    
    // ABSTRACTION: Complex timing analysis hidden behind simple method
    public String getTimingStatus() {
        if (!taken || timeTaken == null || timeScheduled == null) {
            return "N/A";
        }
        
        long minutesDifference = java.time.Duration.between(timeScheduled, timeTaken).toMinutes();
        
        if (Math.abs(minutesDifference) <= 15) {
            return "On Time";
        } else if (minutesDifference > 15) {
            return "Late (" + minutesDifference + " minutes)";
        } else {
            return "Early (" + Math.abs(minutesDifference) + " minutes)";
        }
    }
    
    // ABSTRACTION: Mark medication as taken
    public void markAsTaken() {
        markAsTaken(LocalDateTime.now(), null);
    }
    
    public void markAsTaken(String notes) {
        markAsTaken(LocalDateTime.now(), notes);
    }
    
    public void markAsTaken(LocalDateTime timeTaken, String notes) {
        this.taken = true;
        this.timeTaken = timeTaken;
        this.missedReason = null; // Clear any previous missed reason
        if (notes != null && !notes.trim().isEmpty()) {
            setNotes(notes);
        }
        
        // Update medication statistics if medication reference exists
        if (medication != null) {
            medication.takeMedication();
        }
    }
    
    // ABSTRACTION: Mark medication as missed
    public void markAsMissed(String reason) {
        this.taken = false;
        this.timeTaken = null;
        this.missedReason = reason != null ? reason.trim() : "";
        
        // Update medication statistics if medication reference exists
        if (medication != null) {
            medication.missedMedication();
        }
    }
    
    // ABSTRACTION: Check if medication was taken on time
    public boolean wasTakenOnTime() {
        if (!taken || timeTaken == null || timeScheduled == null) {
            return false;
        }
        
        long minutesDifference = Math.abs(java.time.Duration.between(timeScheduled, timeTaken).toMinutes());
        return minutesDifference <= 15; // Within 15 minutes is considered on time
    }
    
    // ABSTRACTION: Check if medication is overdue
    public boolean isOverdue() {
        if (taken || timeScheduled == null) {
            return false;
        }
        
        return LocalDateTime.now().isAfter(timeScheduled.plusMinutes(30)); // 30 minutes grace period
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
    
    public void setTimeScheduled(LocalDateTime timeScheduled) {
        this.timeScheduled = timeScheduled;
    }
    
    public void setMissedReason(String missedReason) {
        this.missedReason = missedReason != null ? missedReason.trim() : "";
    }
    
    // Getters (ENCAPSULATION: Controlled read access)
    public String getMedicationName() {
        return medicationName;
    }
    
    public String getDosage() {
        return dosage;
    }
    
    public LocalDateTime getTimeScheduled() {
        return timeScheduled;
    }
    
    public LocalDateTime getTimeTaken() {
        return timeTaken;
    }
    
    public boolean isTaken() {
        return taken;
    }
    
    public String getMissedReason() {
        return missedReason;
    }
    
    public Medication getMedication() {
        return medication;
    }
    
    // Setters for JPA
    public void setTimeTaken(LocalDateTime timeTaken) {
        this.timeTaken = timeTaken;
    }
    
    public void setTaken(boolean taken) {
        this.taken = taken;
    }
    
    public void setMedication(Medication medication) {
        this.medication = medication;
        if (medication != null) {
            this.medicationName = medication.getName();
            this.dosage = medication.getDosage();
        }
    }
    
    @Override
    public String toString() {
        return "MedicationRecord{" +
                "id=" + id +
                ", medicationName='" + medicationName + '\'' +
                ", dosage='" + dosage + '\'' +
                ", taken=" + taken +
                ", timeScheduled=" + (timeScheduled != null ? timeScheduled.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "null") +
                ", timingStatus='" + getTimingStatus() + '\'' +
                '}';
    }
}

