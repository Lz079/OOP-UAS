package com.chronicare.service;

import com.chronicare.model.User;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Service class for User management in ChroniCare application.
 * Demonstrates ABSTRACTION by hiding complex business logic from controllers.
 * Uses in-memory storage for demonstration (can be replaced with JPA repository).
 */
@Service
public class UserService {
    
    // ENCAPSULATION: Private data storage
    private final Map<Long, User> users = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    // Constructor with sample data
    public UserService() {
        initializeSampleData();
    }
    
    // ABSTRACTION: Hide complex initialization logic
    private void initializeSampleData() {
        // Create sample users for demonstration
        User user1 = new User("Sarah Johnson", "Diabetes Type 2, Hypertension", "sarah.johnson@email.com", "+1-555-0123");
        user1.setId(idGenerator.getAndIncrement());
        users.put(user1.getId(), user1);
        
        User user2 = new User("Michael Chen", "Asthma, High Cholesterol", "michael.chen@email.com", "+1-555-0124");
        user2.setId(idGenerator.getAndIncrement());
        users.put(user2.getId(), user2);
        
        User user3 = new User("Emily Rodriguez", "Rheumatoid Arthritis", "emily.rodriguez@email.com", "+1-555-0125");
        user3.setId(idGenerator.getAndIncrement());
        users.put(user3.getId(), user3);
        
        System.out.println("Initialized " + users.size() + " sample users");
    }
    
    /**
     * Get all users
     * ABSTRACTION: Simple interface for complex data retrieval
     */
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }
    
    /**
     * Get user by ID
     * ABSTRACTION: Hide lookup complexity
     */
    public Optional<User> getUserById(Long id) {
        return Optional.ofNullable(users.get(id));
    }
    
    /**
     * Save user (create or update)
     * ABSTRACTION: Hide persistence logic
     */
    public User saveUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        
        // Validate user data (ENCAPSULATION in action)
        validateUser(user);
        
        // Assign ID if new user
        if (user.getId() == null) {
            user.setId(idGenerator.getAndIncrement());
        }
        
        users.put(user.getId(), user);
        System.out.println("User saved: " + user.getName() + " (ID: " + user.getId() + ")");
        
        return user;
    }
    
    /**
     * Delete user by ID
     * ABSTRACTION: Hide deletion complexity
     */
    public void deleteUser(Long id) {
        User removedUser = users.remove(id);
        if (removedUser != null) {
            System.out.println("User deleted: " + removedUser.getName() + " (ID: " + id + ")");
        }
    }
    
    /**
     * Search users by name or condition
     * ABSTRACTION: Hide search algorithm complexity
     */
    public List<User> searchUsers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllUsers();
        }
        
        String lowerQuery = query.toLowerCase().trim();
        
        return users.values().stream()
            .filter(user -> 
                user.getName().toLowerCase().contains(lowerQuery) ||
                user.getCondition().toLowerCase().contains(lowerQuery) ||
                (user.getEmail() != null && user.getEmail().toLowerCase().contains(lowerQuery))
            )
            .collect(Collectors.toList());
    }
    
    /**
     * Get users by condition
     * ABSTRACTION: Hide filtering logic
     */
    public List<User> getUsersByCondition(String condition) {
        if (condition == null || condition.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String lowerCondition = condition.toLowerCase().trim();
        
        return users.values().stream()
            .filter(user -> user.getCondition().toLowerCase().contains(lowerCondition))
            .collect(Collectors.toList());
    }
    
    /**
     * Get user statistics
     * ABSTRACTION: Hide complex statistical calculations
     */
    public Object getUserStatistics() {
        List<User> allUsers = getAllUsers();
        
        // Calculate various statistics using OOP principles
        Map<String, Long> conditionCounts = allUsers.stream()
            .flatMap(user -> Arrays.stream(user.getCondition().split(","))
                .map(String::trim))
            .collect(Collectors.groupingBy(
                condition -> condition,
                Collectors.counting()
            ));
        
        double averageAdherence = allUsers.stream()
            .mapToDouble(User::calculateOverallAdherence)
            .average()
            .orElse(0.0);
        
        long totalActiveMedications = allUsers.stream()
            .mapToLong(User::getActiveMedicationCount)
            .sum();
        
        long totalMedications = allUsers.stream()
            .mapToLong(User::getTotalMedicationCount)
            .sum();
        
        // Return statistics object
        final int userCount = allUsers.size();
        final double avgAdherence = averageAdherence;
        final long activeMeds = totalActiveMedications;
        final long totalMeds = totalMedications;
        
        return new Object() {
            public final int totalUsers = userCount;
            public final Map<String, Long> conditionDistribution = conditionCounts;
            public final double averageAdherence = avgAdherence;
            public final String averageAdherencePercentage = String.format("%.1f%%", avgAdherence * 100);
            public final long totalActiveMedications = activeMeds;
            public final long totalMedications = totalMeds;
            public final double averageMedicationsPerUser = allUsers.isEmpty() ? 0.0 : 
                (double) totalMeds / allUsers.size();
            public final long usersWithExcellentAdherence = allUsers.stream()
                .mapToLong(user -> user.calculateOverallAdherence() >= 0.9 ? 1 : 0)
                .sum();
            public final long usersWithPoorAdherence = allUsers.stream()
                .mapToLong(user -> user.calculateOverallAdherence() < 0.7 ? 1 : 0)
                .sum();
        };
    }
    
    /**
     * Get users with poor adherence
     * ABSTRACTION: Hide filtering and calculation logic
     */
    public List<User> getUsersWithPoorAdherence() {
        return users.values().stream()
            .filter(user -> user.calculateOverallAdherence() < 0.7)
            .collect(Collectors.toList());
    }
    
    /**
     * Get users with excellent adherence
     * ABSTRACTION: Hide filtering and calculation logic
     */
    public List<User> getUsersWithExcellentAdherence() {
        return users.values().stream()
            .filter(user -> user.calculateOverallAdherence() >= 0.9)
            .collect(Collectors.toList());
    }
    
    /**
     * Check if user exists
     * ABSTRACTION: Simple existence check
     */
    public boolean userExists(Long id) {
        return users.containsKey(id);
    }
    
    /**
     * Get user count
     * ABSTRACTION: Simple count operation
     */
    public long getUserCount() {
        return users.size();
    }
    
    /**
     * Validate user data
     * ENCAPSULATION: Private validation logic
     */
    private void validateUser(User user) {
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("User name cannot be null or empty");
        }
        
        if (user.getCondition() == null || user.getCondition().trim().isEmpty()) {
            throw new IllegalArgumentException("User condition cannot be null or empty");
        }
        
        // Email validation if provided
        if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
            if (!user.getEmail().contains("@") || !user.getEmail().contains(".")) {
                throw new IllegalArgumentException("Invalid email format");
            }
        }
        
        // Phone validation if provided
        if (user.getPhone() != null && !user.getPhone().trim().isEmpty()) {
            String cleanPhone = user.getPhone().replaceAll("[^0-9]", "");
            if (cleanPhone.length() < 10) {
                throw new IllegalArgumentException("Phone number must have at least 10 digits");
            }
        }
    }
    
    /**
     * Update user's last activity
     * ABSTRACTION: Hide timestamp management
     */
    public void updateUserActivity(Long userId) {
        User user = users.get(userId);
        if (user != null) {
            // In a real application, this would update a lastActivity timestamp
            System.out.println("Updated activity for user: " + user.getName());
        }
    }
    
    /**
     * Get users requiring attention (poor adherence or no recent activity)
     * ABSTRACTION: Hide complex business logic for identifying at-risk users
     */
    public List<User> getUsersRequiringAttention() {
        return users.values().stream()
            .filter(user -> {
                double adherence = user.calculateOverallAdherence();
                boolean poorAdherence = adherence < 0.7;
                boolean noMedications = user.getTotalMedicationCount() == 0;
                
                return poorAdherence || noMedications;
            })
            .collect(Collectors.toList());
    }
}

