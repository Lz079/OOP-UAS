package com.chronicare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot Application class for ChroniCare.
 * Demonstrates ABSTRACTION by hiding Spring Boot's complex initialization.
 */
@SpringBootApplication
public class ChroniCareApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ChroniCareApplication.class, args);
        System.out.println("=== ChroniCare Spring Boot Application Started ===");
        System.out.println("Access the application at: http://localhost:8080");
        System.out.println("API Documentation available at: http://localhost:8080/api");
        System.out.println("Demonstrating Java OOP Principles:");
        System.out.println("• Encapsulation: Private fields with controlled access");
        System.out.println("• Inheritance: Specialized classes extending base classes");
        System.out.println("• Polymorphism: Different implementations of same interface");
        System.out.println("• Abstraction: Complex logic hidden behind simple interfaces");
        System.out.println("================================================");
    }
}

