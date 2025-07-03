package com.chronicare.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Medication entity representing a medication in the ChroniCare system.
 * Demonstrates ENCAPSULATION with private fields and controlled access.
 * Uses JPA annotations for database persistence.
 */
@Entity
@Table(name = "medications")
public class Medication {
    
    // ENCAPSULATION: Private fields with controlled access
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Medication name cannot be blank")
    @Column(nullable = false)
    private String name;
    
    @NotBlank(message = "Dosage cannot be blank")
    @Column(nullable = false)
    private String dosage;
    
    @Positive(message = "Frequency must be positive")
    @Column(name = "frequency_per_day", nullable = false)
    private int frequencyPerDay;
    
    @Column(columnDefinition = "TEXT")
    private String instructions;
    
    @Column(name = "doses_taken")
    private int dosesTaken = 0;
    
    @Column(name = "doses_missed")
    private int dosesMissed = 0;
    
    @Column(name = "total_doses_recorded")
    private int totalDosesRecorded = 0;
    
    @Column(name = "is_active")
    private boolean active = true;
    
    @Column(name = "created_date")
    private LocalDateTime createdDate;
    
    @Column(name = "last_taken")
    private LocalDateTime lastTaken;
    
    // Many-to-One relationship with User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    // One-to-Many relationship with MedicationRecord
    @OneToMany(mappedBy = "medication", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MedicationRecord> records = new ArrayList<>();
    
    // Transient field for schedule times (calculated, not stored)
    @Transient
    private List<String> scheduleTimes = new ArrayList<>();
    
    // Default constructor for JPA
    public Medication() {
        this.createdDate = LocalDateTime.now();
    }
    
    // Constructor with validation
    public Medication(String name, String dosage, int frequencyPerDay, String instructions) {
        this();
        setName(name);
        setDosage(dosage);
        setFrequencyPerDay(frequencyPerDay);
        setInstructions(instructions);
        generateDefaultSchedule();
    }
    
    // ENCAPSULATION: Controlled access with validation
    public void setName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name.trim();
        } else {
            throw new IllegalArgumentException("Medication name cannot be null or empty");
        }
    }
    
    public void setDosage(String dosage) {
        if (dosage != null && !dosage.trim().isEmpty()) {
            this.dosage = dosage.trim();
        } else {
            throw new IllegalArgumentException("Dosage cannot be null or empty");
        }
    }
    
    public void setFrequencyPerDay(int frequencyPerDay) {
        if (frequencyPerDay > 0 && frequencyPerDay <= 24) {
            this.frequencyPerDay = frequencyPerDay;
            generateDefaultSchedule(); // Regenerate schedule when frequency changes
        } else {
            throw new IllegalArgumentException("Frequency must be between 1 and 24 times per day");
        }
    }
    
    public void setInstructions(String instructions) {
        this.instructions = instructions != null ? instructions.trim() : "";
    }
    
    // ABSTRACTION: Complex scheduling logic hidden behind simple method
    private void generateDefaultSchedule() {
        scheduleTimes.clear();
        
        switch (frequencyPerDay) {
            case 1:
                scheduleTimes.add("08:00");
                break;
            case 2:
                scheduleTimes.add("08:00");
                scheduleTimes.add("20:00");
                break;
            case 3:
                scheduleTimes.add("08:00");
                scheduleTimes.add("14:00");
                scheduleTimes.add("20:00");
                break;
            case 4:
                scheduleTimes.add("08:00");
                scheduleTimes.add("12:00");
                scheduleTimes.add("16:00");
                scheduleTimes.add("20:00");
                break;
            default:
                // Generate evenly spaced times for higher frequencies
                int interval = 24 / frequencyPerDay;
                for (int i = 0; i < frequencyPerDay; i++) {
                    int hour = 8 + (i * interval);
                    if (hour >= 24) hour = hour - 24;
                    scheduleTimes.add(String.format("%02d:00", hour));
                }
        }
    }
    
    // ABSTRACTION: Simple interface for medication adherence tracking
    public void takeMedication() {
        dosesTaken++;
        totalDosesRecorded++;
        lastTaken = LocalDateTime.now();
        System.out.println("Medication taken: " + name + " (" + dosage + ")");
    }
    
    public void missedMedication() {
        dosesMissed++;
        totalDosesRecorded++;
        System.out.println("Medication missed: " + name + " (" + dosage + ")");
    }
    
    // ABSTRACTION: Complex calculation hidden behind simple method
    public double getAdherenceRate() {
        if (totalDosesRecorded == 0) {
            return 0.0;
        }
        return (double) dosesTaken / totalDosesRecorded;
    }
    
    // ABSTRACTION: Get adherence percentage as formatted string
    public String getAdherencePercentage() {
        return String.format("%.1f%%", getAdherenceRate() * 100);
    }
    
    // ABSTRACTION: Determine adherence status
    public String getAdherenceStatus() {
        double rate = getAdherenceRate();
        if (rate >= 0.9) return "Excellent";
        if (rate >= 0.8) return "Good";
        if (rate >= 0.7) return "Fair";
        return "Poor";
    }
    
    // ABSTRACTION: Check if medication is due soon
    public boolean isDueSoon() {
        if (lastTaken == null) return true;
        
        // Simple logic: due if more than 12 hours since last taken for daily meds
        LocalDateTime now = LocalDateTime.now();
        long hoursSinceLastTaken = java.time.Duration.between(lastTaken, now).toHours();
        
        return hoursSinceLastTaken >= (24 / frequencyPerDay);
    }
    
    // ABSTRACTION: Get next scheduled time
    public String getNextScheduledTime() {
        if (scheduleTimes.isEmpty()) {
            generateDefaultSchedule();
        }
        
        // Simple implementation - return first scheduled time
        // In a real application, this would calculate based on current time
        return scheduleTimes.isEmpty() ? "Not scheduled" : scheduleTimes.get(0);
    }
    
    // Getters (ENCAPSULATION: Controlled read access)
    public Long getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDosage() {
        return dosage;
    }
    
    public int getFrequencyPerDay() {
        return frequencyPerDay;
    }
    
    public String getInstructions() {
        return instructions;
    }
    
    public int getDosesTaken() {
        return dosesTaken;
    }
    
    public int getDosesMissed() {
        return dosesMissed;
    }
    
    public int getTotalDosesRecorded() {
        return totalDosesRecorded;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
    
    public LocalDateTime getLastTaken() {
        return lastTaken;
    }
    
    public User getUser() {
        return user;
    }
    
    public List<String> getScheduleTimes() {
        if (scheduleTimes.isEmpty()) {
            generateDefaultSchedule();
        }
        return new ArrayList<>(scheduleTimes);
    }
    
    public List<MedicationRecord> getRecords() {
        return new ArrayList<>(records);
    }
    
    // Setters for JPA
    public void setId(Long id) {
        this.id = id;
    }
    
    public void setDosesTaken(int dosesTaken) {
        this.dosesTaken = dosesTaken;
    }
    
    public void setDosesMissed(int dosesMissed) {
        this.dosesMissed = dosesMissed;
    }
    
    public void setTotalDosesRecorded(int totalDosesRecorded) {
        this.totalDosesRecorded = totalDosesRecorded;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
    
    public void setLastTaken(LocalDateTime lastTaken) {
        this.lastTaken = lastTaken;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public void setRecords(List<MedicationRecord> records) {
        this.records = records != null ? records : new ArrayList<>();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Medication that = (Medication) obj;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
    
    @Override
    public String toString() {
        return "Medication{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", dosage='" + dosage + '\'' +
                ", frequencyPerDay=" + frequencyPerDay +
                ", adherenceRate=" + getAdherencePercentage() +
                ", active=" + active +
                '}';
    }
}

