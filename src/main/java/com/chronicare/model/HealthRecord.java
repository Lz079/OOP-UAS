package com.chronicare.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Abstract base class for all health records in the ChroniCare system.
 * Demonstrates ABSTRACTION and provides foundation for INHERITANCE.
 * Uses JPA inheritance strategy for database persistence.
 */
@Entity
@Table(name = "health_records")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "record_type", discriminatorType = DiscriminatorType.STRING)
public abstract class HealthRecord {
    
    // ENCAPSULATION: Protected fields accessible to subclasses
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;
    
    @Column(name = "record_type", insertable = false, updatable = false)
    protected String recordType;
    
    @Column(name = "created_date", nullable = false)
    protected LocalDateTime createdDate;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    protected String notes;
    
    @Column(name = "priority")
    protected String priority = "NORMAL";
    
    // Many-to-One relationship with User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    protected User user;
    
    // Default constructor for JPA
    public HealthRecord() {
        this.createdDate = LocalDateTime.now();
    }
    
    // Constructor with basic fields
    public HealthRecord(String recordType) {
        this();
        this.recordType = recordType;
    }
    
    // Constructor with notes
    public HealthRecord(String recordType, String notes) {
        this(recordType);
        setNotes(notes);
    }
    
    // ABSTRACTION: Abstract method that subclasses must implement
    public abstract String getSummary();
    
    // ABSTRACTION: Abstract method for record-specific details
    public abstract String getDetails();
    
    // ENCAPSULATION: Controlled access with validation
    public void setNotes(String notes) {
        this.notes = notes != null ? notes.trim() : "";
    }
    
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
    
    // ABSTRACTION: Simple interface for priority checking
    public boolean isHighPriority() {
        return "HIGH".equals(priority) || "URGENT".equals(priority);
    }
    
    public boolean isUrgent() {
        return "URGENT".equals(priority);
    }
    
    // ABSTRACTION: Calculate age of record
    public long getAgeInDays() {
        return java.time.Duration.between(createdDate, LocalDateTime.now()).toDays();
    }
    
    public long getAgeInHours() {
        return java.time.Duration.between(createdDate, LocalDateTime.now()).toHours();
    }
    
    // ABSTRACTION: Format creation date for display
    public String getFormattedCreatedDate() {
        return createdDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
    
    // Getters (ENCAPSULATION: Controlled read access)
    public Long getId() {
        return id;
    }
    
    public String getRecordType() {
        return recordType;
    }
    
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public String getPriority() {
        return priority;
    }
    
    public User getUser() {
        return user;
    }
    
    // Setters for JPA
    public void setId(Long id) {
        this.id = id;
    }
    
    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }
    
    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        HealthRecord that = (HealthRecord) obj;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
    
    @Override
    public String toString() {
        return "HealthRecord{" +
                "id=" + id +
                ", recordType='" + recordType + '\'' +
                ", createdDate=" + getFormattedCreatedDate() +
                ", priority='" + priority + '\'' +
                '}';
    }
}

