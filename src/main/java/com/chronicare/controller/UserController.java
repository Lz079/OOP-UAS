package com.chronicare.controller;

import com.chronicare.model.User;
import com.chronicare.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * REST Controller for User management in ChroniCare application.
 * Demonstrates Spring Boot REST API implementation with proper HTTP methods.
 * Uses dependency injection (ABSTRACTION) to hide service implementation details.
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*") // Allow CORS for frontend integration
public class UserController {
    
    // ABSTRACTION: Service layer hides complex business logic
    private final UserService userService;
    
    // DEPENDENCY INJECTION: Spring automatically injects UserService
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    /**
     * Get all users
     * GET /api/users
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get user by ID
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        try {
            Optional<User> user = userService.getUserById(id);
            return user.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Create new user
     * POST /api/users
     */
    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        try {
            User savedUser = userService.saveUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Update existing user
     * PUT /api/users/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @Valid @RequestBody User userDetails) {
        try {
            Optional<User> existingUser = userService.getUserById(id);
            if (existingUser.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            User user = existingUser.get();
            user.setName(userDetails.getName());
            user.setCondition(userDetails.getCondition());
            user.setEmail(userDetails.getEmail());
            user.setPhone(userDetails.getPhone());
            
            User updatedUser = userService.saveUser(user);
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Delete user
     * DELETE /api/users/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        try {
            if (userService.getUserById(id).isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get user's medication adherence summary
     * GET /api/users/{id}/adherence
     */
    @GetMapping("/{id}/adherence")
    public ResponseEntity<Object> getUserAdherence(@PathVariable Long id) {
        try {
            Optional<User> userOpt = userService.getUserById(id);
            if (userOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            User user = userOpt.get();
            
            // Create adherence summary using OOP principles
            var adherenceData = new Object() {
                public final String userName = user.getName();
                public final String condition = user.getCondition();
                public final double overallAdherence = user.calculateOverallAdherence();
                public final String adherencePercentage = String.format("%.1f%%", overallAdherence * 100);
                public final long activeMedications = user.getActiveMedicationCount();
                public final long totalMedications = user.getTotalMedicationCount();
                public final String status = overallAdherence >= 0.9 ? "Excellent" :
                                           overallAdherence >= 0.8 ? "Good" :
                                           overallAdherence >= 0.7 ? "Fair" : "Poor";
            };
            
            return ResponseEntity.ok(adherenceData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Search users by name or condition
     * GET /api/users/search?query={query}
     */
    @GetMapping("/search")
    public ResponseEntity<List<User>> searchUsers(@RequestParam String query) {
        try {
            List<User> users = userService.searchUsers(query);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get users by condition
     * GET /api/users/condition/{condition}
     */
    @GetMapping("/condition/{condition}")
    public ResponseEntity<List<User>> getUsersByCondition(@PathVariable String condition) {
        try {
            List<User> users = userService.getUsersByCondition(condition);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get user statistics
     * GET /api/users/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Object> getUserStats() {
        try {
            var stats = userService.getUserStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

