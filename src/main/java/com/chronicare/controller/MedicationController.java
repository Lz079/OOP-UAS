package com.chronicare.controller;

import com.chronicare.model.Medication;
import com.chronicare.model.User;
import com.chronicare.service.MedicationService;
import com.chronicare.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * REST Controller for Medication management in ChroniCare application.
 * Demonstrates Spring Boot REST API with medication-specific operations.
 * Uses ABSTRACTION through service layer to hide complex business logic.
 */
@RestController
@RequestMapping("/api/medications")
@CrossOrigin(origins = "*") // Allow CORS for frontend integration
public class MedicationController {
    
    // ABSTRACTION: Service layers hide implementation details
    private final MedicationService medicationService;
    private final UserService userService;
    
    // DEPENDENCY INJECTION: Spring automatically injects services
    @Autowired
    public MedicationController(MedicationService medicationService, UserService userService) {
        this.medicationService = medicationService;
        this.userService = userService;
    }
    
    /**
     * Get all medications for a specific user
     * GET /api/medications/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Medication>> getMedicationsByUser(@PathVariable Long userId) {
        try {
            Optional<User> user = userService.getUserById(userId);
            if (user.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            List<Medication> medications = medicationService.getMedicationsByUser(userId);
            return ResponseEntity.ok(medications);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get medication by ID
     * GET /api/medications/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Medication> getMedicationById(@PathVariable Long id) {
        try {
            Optional<Medication> medication = medicationService.getMedicationById(id);
            return medication.map(ResponseEntity::ok)
                           .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Create new medication for a user
     * POST /api/medications/user/{userId}
     */
    @PostMapping("/user/{userId}")
    public ResponseEntity<Medication> createMedication(@PathVariable Long userId, 
                                                     @Valid @RequestBody Medication medication) {
        try {
            Optional<User> user = userService.getUserById(userId);
            if (user.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            medication.setUser(user.get());
            Medication savedMedication = medicationService.saveMedication(medication);
            
            // Add medication to user (demonstrates OOP relationship)
            user.get().addMedication(savedMedication);
            userService.saveUser(user.get());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(savedMedication);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Update existing medication
     * PUT /api/medications/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Medication> updateMedication(@PathVariable Long id, 
                                                     @Valid @RequestBody Medication medicationDetails) {
        try {
            Optional<Medication> existingMedication = medicationService.getMedicationById(id);
            if (existingMedication.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Medication medication = existingMedication.get();
            medication.setName(medicationDetails.getName());
            medication.setDosage(medicationDetails.getDosage());
            medication.setFrequencyPerDay(medicationDetails.getFrequencyPerDay());
            medication.setInstructions(medicationDetails.getInstructions());
            medication.setActive(medicationDetails.isActive());
            
            Medication updatedMedication = medicationService.saveMedication(medication);
            return ResponseEntity.ok(updatedMedication);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Delete medication
     * DELETE /api/medications/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedication(@PathVariable Long id) {
        try {
            Optional<Medication> medication = medicationService.getMedicationById(id);
            if (medication.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            // Remove from user's medication list (demonstrates OOP relationship)
            User user = medication.get().getUser();
            if (user != null) {
                user.removeMedication(medication.get());
                userService.saveUser(user);
            }
            
            medicationService.deleteMedication(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Mark medication as taken
     * POST /api/medications/{id}/take
     */
    @PostMapping("/{id}/take")
    public ResponseEntity<Object> takeMedication(@PathVariable Long id) {
        try {
            Optional<Medication> medicationOpt = medicationService.getMedicationById(id);
            if (medicationOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Medication medication = medicationOpt.get();
            medication.takeMedication(); // Demonstrates ENCAPSULATION
            medicationService.saveMedication(medication);
            
            // Return updated adherence information
            var response = new Object() {
                public final String message = "Medication taken successfully";
                public final String medicationName = medication.getName();
                public final String dosage = medication.getDosage();
                public final int dosesTaken = medication.getDosesTaken();
                public final double adherenceRate = medication.getAdherenceRate();
                public final String adherencePercentage = medication.getAdherencePercentage();
                public final String adherenceStatus = medication.getAdherenceStatus();
            };
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Mark medication as missed
     * POST /api/medications/{id}/miss
     */
    @PostMapping("/{id}/miss")
    public ResponseEntity<Object> missMedication(@PathVariable Long id) {
        try {
            Optional<Medication> medicationOpt = medicationService.getMedicationById(id);
            if (medicationOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Medication medication = medicationOpt.get();
            medication.missedMedication(); // Demonstrates ENCAPSULATION
            medicationService.saveMedication(medication);
            
            // Return updated adherence information
            var response = new Object() {
                public final String message = "Medication marked as missed";
                public final String medicationName = medication.getName();
                public final String dosage = medication.getDosage();
                public final int dosesMissed = medication.getDosesMissed();
                public final double adherenceRate = medication.getAdherenceRate();
                public final String adherencePercentage = medication.getAdherencePercentage();
                public final String adherenceStatus = medication.getAdherenceStatus();
            };
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get medication schedule for a user
     * GET /api/medications/user/{userId}/schedule
     */
    @GetMapping("/user/{userId}/schedule")
    public ResponseEntity<Object> getMedicationSchedule(@PathVariable Long userId) {
        try {
            Optional<User> user = userService.getUserById(userId);
            if (user.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            List<Medication> medications = medicationService.getMedicationsByUser(userId);
            
            // Create schedule using OOP principles
            var schedule = medications.stream()
                .filter(Medication::isActive)
                .map(med -> new Object() {
                    public final String name = med.getName();
                    public final String dosage = med.getDosage();
                    public final int frequency = med.getFrequencyPerDay();
                    public final List<String> times = med.getScheduleTimes();
                    public final String nextDue = med.getNextScheduledTime();
                    public final boolean isDueSoon = med.isDueSoon();
                    public final double adherence = med.getAdherenceRate();
                    public final String adherenceStatus = med.getAdherenceStatus();
                })
                .toList();
            
            var response = new Object() {
                public final String userName = user.get().getName();
                public final List<?> medications = schedule;
                public final int totalActiveMedications = schedule.size();
                public final double overallAdherence = user.get().calculateOverallAdherence();
            };
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get medication adherence statistics
     * GET /api/medications/{id}/adherence
     */
    @GetMapping("/{id}/adherence")
    public ResponseEntity<Object> getMedicationAdherence(@PathVariable Long id) {
        try {
            Optional<Medication> medicationOpt = medicationService.getMedicationById(id);
            if (medicationOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Medication medication = medicationOpt.get();
            
            // Demonstrate ABSTRACTION - complex calculations hidden behind simple methods
            var adherenceData = new Object() {
                public final String medicationName = medication.getName();
                public final String dosage = medication.getDosage();
                public final int dosesTaken = medication.getDosesTaken();
                public final int dosesMissed = medication.getDosesMissed();
                public final int totalDoses = medication.getTotalDosesRecorded();
                public final double adherenceRate = medication.getAdherenceRate();
                public final String adherencePercentage = medication.getAdherencePercentage();
                public final String adherenceStatus = medication.getAdherenceStatus();
                public final boolean isDueSoon = medication.isDueSoon();
                public final String nextScheduledTime = medication.getNextScheduledTime();
                public final List<String> scheduleTimes = medication.getScheduleTimes();
            };
            
            return ResponseEntity.ok(adherenceData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Search medications by name
     * GET /api/medications/search?query={query}
     */
    @GetMapping("/search")
    public ResponseEntity<List<Medication>> searchMedications(@RequestParam String query) {
        try {
            List<Medication> medications = medicationService.searchMedications(query);
            return ResponseEntity.ok(medications);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get all active medications
     * GET /api/medications/active
     */
    @GetMapping("/active")
    public ResponseEntity<List<Medication>> getActiveMedications() {
        try {
            List<Medication> medications = medicationService.getActiveMedications();
            return ResponseEntity.ok(medications);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

