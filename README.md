# ChroniCare: Chronic Condition Management System

## Project Overview

ChroniCare is a comprehensive web application designed to assist individuals in managing their chronic health conditions. It provides features for tracking medications, managing user profiles, and receiving important notifications. The project is built with a focus on demonstrating core Object-Oriented Programming (OOP) principles in a practical, real-world healthcare context.

**Developed by:** Ahmed Masoud Baghni
**NIM:** 20230040030

**Documentation + Video Link:** https://drive.google.com/drive/folders/1UyiwScUKNQYJ8e8Za476YX9lsPZfqsVD

## Features

### Core Functionality
*   **User Management:** Create, view, update, and delete user profiles, including personal details, chronic conditions, and emergency contacts.
*   **Medication Tracking:** Add, manage, and track medications, including dosage, frequency, instructions, and adherence.
*   **Notification System:** Receive reminders for medications and appointments.
*   **Dashboard:** An overview of key statistics such as total users, active medications, and adherence rates.

### Object-Oriented Programming (OOP) Demonstrations
ChroniCare is meticulously designed to showcase the four pillars of OOP:

1.  **Encapsulation:**
    *   **Concept:** Bundling data (attributes) and methods that operate on the data within a single unit (class), and restricting direct access to some of the object's components.
    *   **Implementation:** Demonstrated in `User` and `Medication` classes where private fields are accessed and modified only through public getter and setter methods. These methods often include validation logic to ensure data integrity (e.g., email format validation, dosage format validation).

2.  **Inheritance:**
    *   **Concept:** A mechanism where one class (subclass) acquires the properties and behaviors of another class (superclass), promoting code reusability and establishing a hierarchical relationship.
    *   **Implementation:** The `HealthRecord` class serves as a base class, defining common attributes for health-related entries. `MedicationRecord` inherits from `HealthRecord`, extending its functionality with specific attributes relevant to medication doses (taken/missed).

3.  **Polymorphism:**
    *   **Concept:** The ability of different classes to be treated as instances of a common superclass, allowing a single interface to represent different underlying types or behaviors.
    *   **Implementation:** The `Notification` base class defines a common interface (`send()`, `get_content()`). `MedicationNotification` and `AppointmentNotification` are concrete subclasses that implement these methods differently, providing specific content and behavior for each notification type. The system can then process a list of `Notification` objects uniformly, invoking the correct `send()` method at runtime.

4.  **Abstraction:**
    *   **Concept:** Hiding complex implementation details and showing only the essential features of an object, focusing on what an object does rather than how it does it.
    *   **Implementation:** The service layer (`UserService`, `MedicationService`, `NotificationService`) provides abstract interfaces for complex business logic. For example, the frontend interacts with `UserService.calculateAdherence()` without needing to know the intricate details of how adherence is computed, thus abstracting away the complexity of data retrieval and calculations.
