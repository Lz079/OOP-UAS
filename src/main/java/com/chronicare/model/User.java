package com.chronicare.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * User entity representing a patient in the ChroniCare system.
 * Demonstrates ENCAPSULATION with private fields and controlled access.
 * Uses JPA annotations for database persistence.
 */
@Entity
@Table(name = "users")
public class User {
    
    // ENCAPSULATION: Private fields protect data integrity
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Name cannot be blank")
    @Column(nullable = false)
    private String name;
    
    @NotBlank(message = "Condition cannot be blank")
    @Column(nullable = false)
    private String condition;
    
    @Column(name = "email")
    private String email;
    
    @Column(name = "phone")
    private String phone;
    
    // One-to-Many relationship with Medication
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Medication> medications = new ArrayList<>();
    
    // One-to-Many relationship with HealthRecord
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<HealthRecord> healthRecords = new ArrayList<>();
    
    // Default constructor for JPA
    public User() {}
    
    // Constructor with validation
    public User(String name, String condition) {
        setName(name);
        setCondition(condition);
    }
    
    // Constructor with all fields
    public User(String name, String condition, String email, String phone) {
        setName(name);
        setCondition(condition);
        setEmail(email);
        setPhone(phone);
    }
    
    // ENCAPSULATION: Controlled access with validation
    public void setName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name.trim();
        } else {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
    }
    
    public void setCondition(String condition) {
        if (condition != null && !condition.trim().isEmpty()) {
            this.condition = condition.trim();
        } else {
            throw new IllegalArgumentException("Condition cannot be null or empty");
        }
    }
    
    public void setEmail(String email) {
        if (email != null && !email.trim().isEmpty()) {
            // Basic email validation
            if (email.contains("@") && email.contains(".")) {
                this.email = email.trim().toLowerCase();
            } else {
                throw new IllegalArgumentException("Invalid email format");
            }
        }
    }
    
    public void setPhone(String phone) {
        if (phone != null && !phone.trim().isEmpty()) {
            // Remove non-numeric characters for storage
            this.phone = phone.replaceAll("[^0-9+\\-\\s]", "");
        }
    }
    
    // ABSTRACTION: Simple interface for complex medication management
    public void addMedication(Medication medication) {
        if (medication != null && !medications.contains(medication)) {
            medications.add(medication);
            medication.setUser(this);
            System.out.println("Medication " + medication.getName() + " added for " + name);
        }
    }
    
    public void removeMedication(Medication medication) {
        if (medication != null && medications.contains(medication)) {
            medications.remove(medication);
            medication.setUser(null);
            System.out.println("Medication " + medication.getName() + " removed for " + name);
        }
    }
    
    // ABSTRACTION: Simple interface for health record management
    public void addHealthRecord(HealthRecord record) {
        if (record != null && !healthRecords.contains(record)) {
            healthRecords.add(record);
            record.setUser(this);
            System.out.println("Health record added for " + name);
        }
    }
    
    // ABSTRACTION: Calculate overall medication adherence
    public double calculateOverallAdherence() {
        if (medications.isEmpty()) {
            return 0.0;
        }
        
        double totalAdherence = 0.0;
        for (Medication medication : medications) {
            totalAdherence += medication.getAdherenceRate();
        }
        
        return totalAdherence / medications.size();
    }
    
    // ABSTRACTION: Get medication count by status
    public long getActiveMedicationCount() {
        return medications.stream().filter(Medication::isActive).count();
    }
    
    public long getTotalMedicationCount() {
        return medications.size();
    }
    
    // Getters (ENCAPSULATION: Controlled read access)
    public Long getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getCondition() {
        return condition;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getPhone() {
        return phone;
    }
    
    // Return defensive copies to prevent external modification
    public List<Medication> getMedications() {
        return new ArrayList<>(medications);
    }
    
    public List<HealthRecord> getHealthRecords() {
        return new ArrayList<>(healthRecords);
    }
    
    // Setters for JPA
    public void setId(Long id) {
        this.id = id;
    }
    
    public void setMedications(List<Medication> medications) {
        this.medications = medications != null ? medications : new ArrayList<>();
    }
    
    public void setHealthRecords(List<HealthRecord> healthRecords) {
        this.healthRecords = healthRecords != null ? healthRecords : new ArrayList<>();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return id != null && id.equals(user.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", condition='" + condition + '\'' +
                ", email='" + email + '\'' +
                ", medicationCount=" + medications.size() +
                '}';
    }
}

