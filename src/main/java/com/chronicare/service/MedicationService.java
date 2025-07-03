package com.chronicare.service;

import com.chronicare.model.Medication;
import com.chronicare.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Service class for Medication management in ChroniCare application.
 * Demonstrates ABSTRACTION by hiding complex business logic from controllers.
 * Uses in-memory storage for demonstration (can be replaced with JPA repository).
 */
@Service
public class MedicationService {
    
    // ENCAPSULATION: Private data storage
    private final Map<Long, Medication> medications = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    // DEPENDENCY INJECTION: UserService for user operations
    private final UserService userService;
    
    @Autowired
    public MedicationService(UserService userService) {
        this.userService = userService;
        initializeSampleData();
    }
    
    // ABSTRACTION: Hide complex initialization logic
    private void initializeSampleData() {
        // Create sample medications for the first user (Sarah Johnson)
        Optional<User> user1 = userService.getUserById(1L);
        if (user1.isPresent()) {
            // Metformin for diabetes
            Medication metformin = new Medication("Metformin", "500mg", 2, "Take with meals to reduce stomach upset");
            metformin.setId(idGenerator.getAndIncrement());
            metformin.setUser(user1.get());
            // Simulate some adherence data
            metformin.setDosesTaken(14);
            metformin.setDosesMissed(7);
            metformin.setTotalDosesRecorded(21);
            medications.put(metformin.getId(), metformin);
            
            // Lisinopril for hypertension
            Medication lisinopril = new Medication("Lisinopril", "10mg", 1, "Take at the same time each day");
            lisinopril.setId(idGenerator.getAndIncrement());
            lisinopril.setUser(user1.get());
            // Simulate excellent adherence
            lisinopril.setDosesTaken(10);
            lisinopril.setDosesMissed(0);
            lisinopril.setTotalDosesRecorded(10);
            medications.put(lisinopril.getId(), lisinopril);
            
            // Aspirin for cardiovascular protection
            Medication aspirin = new Medication("Aspirin", "81mg", 1, "Take with food to prevent stomach irritation");
            aspirin.setId(idGenerator.getAndIncrement());
            aspirin.setUser(user1.get());
            // Simulate good adherence
            aspirin.setDosesTaken(9);
            aspirin.setDosesMissed(1);
            aspirin.setTotalDosesRecorded(10);
            medications.put(aspirin.getId(), aspirin);
            
            // Add medications to user
            user1.get().addMedication(metformin);
            user1.get().addMedication(lisinopril);
            user1.get().addMedication(aspirin);
        }
        
        // Create sample medications for the second user (Michael Chen)
        Optional<User> user2 = userService.getUserById(2L);
        if (user2.isPresent()) {
            // Albuterol for asthma
            Medication albuterol = new Medication("Albuterol", "90mcg", 4, "Use as needed for breathing difficulties");
            albuterol.setId(idGenerator.getAndIncrement());
            albuterol.setUser(user2.get());
            albuterol.setDosesTaken(8);
            albuterol.setDosesMissed(2);
            albuterol.setTotalDosesRecorded(10);
            medications.put(albuterol.getId(), albuterol);
            
            // Atorvastatin for cholesterol
            Medication atorvastatin = new Medication("Atorvastatin", "20mg", 1, "Take in the evening");
            atorvastatin.setId(idGenerator.getAndIncrement());
            atorvastatin.setUser(user2.get());
            atorvastatin.setDosesTaken(7);
            atorvastatin.setDosesMissed(3);
            atorvastatin.setTotalDosesRecorded(10);
            medications.put(atorvastatin.getId(), atorvastatin);
            
            user2.get().addMedication(albuterol);
            user2.get().addMedication(atorvastatin);
        }
        
        System.out.println("Initialized " + medications.size() + " sample medications");
    }
    
    /**
     * Get all medications
     * ABSTRACTION: Simple interface for complex data retrieval
     */
    public List<Medication> getAllMedications() {
        return new ArrayList<>(medications.values());
    }
    
    /**
     * Get medication by ID
     * ABSTRACTION: Hide lookup complexity
     */
    public Optional<Medication> getMedicationById(Long id) {
        return Optional.ofNullable(medications.get(id));
    }
    
    /**
     * Get medications by user ID
     * ABSTRACTION: Hide filtering logic
     */
    public List<Medication> getMedicationsByUser(Long userId) {
        return medications.values().stream()
            .filter(medication -> medication.getUser() != null && 
                   medication.getUser().getId().equals(userId))
            .collect(Collectors.toList());
    }
    
    /**
     * Save medication (create or update)
     * ABSTRACTION: Hide persistence logic
     */
    public Medication saveMedication(Medication medication) {
        if (medication == null) {
            throw new IllegalArgumentException("Medication cannot be null");
        }
        
        // Validate medication data (ENCAPSULATION in action)
        validateMedication(medication);
        
        // Assign ID if new medication
        if (medication.getId() == null) {
            medication.setId(idGenerator.getAndIncrement());
        }
        
        // Set creation date if new
        if (medication.getCreatedDate() == null) {
            medication.setCreatedDate(LocalDateTime.now());
        }
        
        medications.put(medication.getId(), medication);
        System.out.println("Medication saved: " + medication.getName() + " (ID: " + medication.getId() + ")");
        
        return medication;
    }
    
    /**
     * Delete medication by ID
     * ABSTRACTION: Hide deletion complexity
     */
    public void deleteMedication(Long id) {
        Medication removedMedication = medications.remove(id);
        if (removedMedication != null) {
            System.out.println("Medication deleted: " + removedMedication.getName() + " (ID: " + id + ")");
        }
    }
    
    /**
     * Search medications by name
     * ABSTRACTION: Hide search algorithm complexity
     */
    public List<Medication> searchMedications(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllMedications();
        }
        
        String lowerQuery = query.toLowerCase().trim();
        
        return medications.values().stream()
            .filter(medication -> 
                medication.getName().toLowerCase().contains(lowerQuery) ||
                medication.getDosage().toLowerCase().contains(lowerQuery) ||
                (medication.getInstructions() != null && 
                 medication.getInstructions().toLowerCase().contains(lowerQuery))
            )
            .collect(Collectors.toList());
    }
    
    /**
     * Get active medications
     * ABSTRACTION: Hide filtering logic
     */
    public List<Medication> getActiveMedications() {
        return medications.values().stream()
            .filter(Medication::isActive)
            .collect(Collectors.toList());
    }
    
    /**
     * Get medications due soon for a user
     * ABSTRACTION: Hide complex timing calculations
     */
    public List<Medication> getMedicationsDueSoon(Long userId) {
        return getMedicationsByUser(userId).stream()
            .filter(Medication::isActive)
            .filter(Medication::isDueSoon)
            .collect(Collectors.toList());
    }
    
    /**
     * Get medications with poor adherence
     * ABSTRACTION: Hide adherence calculation logic
     */
    public List<Medication> getMedicationsWithPoorAdherence() {
        return medications.values().stream()
            .filter(medication -> medication.getAdherenceRate() < 0.7)
            .collect(Collectors.toList());
    }
    
    /**
     * Get medications with excellent adherence
     * ABSTRACTION: Hide adherence calculation logic
     */
    public List<Medication> getMedicationsWithExcellentAdherence() {
        return medications.values().stream()
            .filter(medication -> medication.getAdherenceRate() >= 0.9)
            .collect(Collectors.toList());
    }
    
    /**
     * Get medication statistics
     * ABSTRACTION: Hide complex statistical calculations
     */
    public Object getMedicationStatistics() {
        List<Medication> allMedications = getAllMedications();
        List<Medication> activeMedications = getActiveMedications();
        
        // Calculate various statistics using OOP principles
        Map<String, Long> medicationCounts = allMedications.stream()
            .collect(Collectors.groupingBy(
                Medication::getName,
                Collectors.counting()
            ));
        
        Map<Integer, Long> frequencyDistribution = allMedications.stream()
            .collect(Collectors.groupingBy(
                Medication::getFrequencyPerDay,
                Collectors.counting()
            ));
        
        double averageAdherence = allMedications.stream()
            .mapToDouble(Medication::getAdherenceRate)
            .average()
            .orElse(0.0);
        
        long totalDosesTaken = allMedications.stream()
            .mapToLong(Medication::getDosesTaken)
            .sum();
        
        long totalDosesMissed = allMedications.stream()
            .mapToLong(Medication::getDosesMissed)
            .sum();
        
        // Return statistics object
        final int totalMedCount = allMedications.size();
        final int activeMedCount = activeMedications.size();
        final double avgAdherence = averageAdherence;
        final long dosesTaken = totalDosesTaken;
        final long dosesMissed = totalDosesMissed;
        final Map<Integer, Long> freqDistribution = frequencyDistribution;
        
        return new Object() {
            public final int totalMedications = totalMedCount;
            public final int activeMedications = activeMedCount;
            public final int inactiveMedications = totalMedCount - activeMedCount;
            public final Map<String, Long> medicationDistribution = medicationCounts;
            public final Map<Integer, Long> frequencyDistribution = freqDistribution;
            public final double averageAdherence = avgAdherence;
            public final String averageAdherencePercentage = String.format("%.1f%%", avgAdherence * 100);
            public final long totalDosesTaken = dosesTaken;
            public final long totalDosesMissed = dosesMissed;
            public final long totalDosesRecorded = dosesTaken + dosesMissed;
            public final long medicationsWithExcellentAdherence = allMedications.stream()
                .mapToLong(med -> med.getAdherenceRate() >= 0.9 ? 1 : 0)
                .sum();
            public final long medicationsWithPoorAdherence = allMedications.stream()
                .mapToLong(med -> med.getAdherenceRate() < 0.7 ? 1 : 0)
                .sum();
        };
    }
    
    /**
     * Get medication schedule for a user
     * ABSTRACTION: Hide complex scheduling logic
     */
    public Object getMedicationScheduleForUser(Long userId) {
        List<Medication> userMedications = getMedicationsByUser(userId);
        
        // Group medications by scheduled times
        Map<String, List<Medication>> schedule = new TreeMap<>();
        
        for (Medication medication : userMedications) {
            if (medication.isActive()) {
                List<String> times = medication.getScheduleTimes();
                for (String time : times) {
                    schedule.computeIfAbsent(time, k -> new ArrayList<>()).add(medication);
                }
            }
        }
        
        // Create schedule response
        var scheduleEntries = schedule.entrySet().stream()
            .map(entry -> new Object() {
                public final String time = entry.getKey();
                public final List<Object> medications = entry.getValue().stream()
                    .map(med -> new Object() {
                        public final String name = med.getName();
                        public final String dosage = med.getDosage();
                        public final String instructions = med.getInstructions();
                        public final boolean isDueSoon = med.isDueSoon();
                        public final double adherenceRate = med.getAdherenceRate();
                    })
                    .collect(Collectors.toList());
            })
            .collect(Collectors.toList());
        
        final int activeMedCount = (int) userMedications.stream()
            .filter(Medication::isActive).count();
        final Long userIdFinal = userId;
        final String nextDueTime = getNextMedicationDue(userMedications);
        
        return new Object() {
            public final Long userId = userIdFinal;
            public final int totalActiveMedications = activeMedCount;
            public final List<?> schedule = scheduleEntries;
            public final String nextDue = nextDueTime;
        };
    }
    
    /**
     * Get next medication due for a list of medications
     * ABSTRACTION: Hide complex timing calculations
     */
    private String getNextMedicationDue(List<Medication> medications) {
        // Simple implementation - in a real app this would be more sophisticated
        Optional<Medication> nextDue = medications.stream()
            .filter(Medication::isActive)
            .filter(Medication::isDueSoon)
            .findFirst();
        
        return nextDue.map(med -> med.getName() + " at " + med.getNextScheduledTime())
                     .orElse("No medications due soon");
    }
    
    /**
     * Check if medication exists
     * ABSTRACTION: Simple existence check
     */
    public boolean medicationExists(Long id) {
        return medications.containsKey(id);
    }
    
    /**
     * Get medication count
     * ABSTRACTION: Simple count operation
     */
    public long getMedicationCount() {
        return medications.size();
    }
    
    /**
     * Get medication count for user
     * ABSTRACTION: Simple count operation for specific user
     */
    public long getMedicationCountForUser(Long userId) {
        return getMedicationsByUser(userId).size();
    }
    
    /**
     * Validate medication data
     * ENCAPSULATION: Private validation logic
     */
    private void validateMedication(Medication medication) {
        if (medication.getName() == null || medication.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Medication name cannot be null or empty");
        }
        
        if (medication.getDosage() == null || medication.getDosage().trim().isEmpty()) {
            throw new IllegalArgumentException("Medication dosage cannot be null or empty");
        }
        
        if (medication.getFrequencyPerDay() <= 0 || medication.getFrequencyPerDay() > 24) {
            throw new IllegalArgumentException("Frequency must be between 1 and 24 times per day");
        }
        
        if (medication.getUser() == null) {
            throw new IllegalArgumentException("Medication must be associated with a user");
        }
    }
    
    /**
     * Update medication adherence
     * ABSTRACTION: Hide adherence tracking complexity
     */
    public void updateMedicationAdherence(Long medicationId, boolean taken) {
        Medication medication = medications.get(medicationId);
        if (medication != null) {
            if (taken) {
                medication.takeMedication();
            } else {
                medication.missedMedication();
            }
            saveMedication(medication);
        }
    }
    
    /**
     * Get medications requiring attention (poor adherence or overdue)
     * ABSTRACTION: Hide complex business logic for identifying problematic medications
     */
    public List<Medication> getMedicationsRequiringAttention() {
        return medications.values().stream()
            .filter(medication -> {
                boolean poorAdherence = medication.getAdherenceRate() < 0.7;
                boolean overdue = medication.isDueSoon();
                
                return medication.isActive() && (poorAdherence || overdue);
            })
            .collect(Collectors.toList());
    }
}

