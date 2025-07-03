# ChroniCare - Java Spring Boot OOP Application

## 🏥 Overview

ChroniCare is a comprehensive chronic condition management system built with **Java Spring Boot** that demonstrates all four fundamental Object-Oriented Programming principles:

- 🔒 **Encapsulation**: Private fields with controlled access
- 🧬 **Inheritance**: HealthRecord and Notification class hierarchies  
- 🎭 **Polymorphism**: Different notification types with same interface
- 🎨 **Abstraction**: Service layer hiding complex business logic

## 🚀 Quick Start

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher

### Run the Application
```bash
# Build and run
mvn spring-boot:run

# Access the application
# Web Interface: http://localhost:8080
# API Documentation: http://localhost:8080/api
# H2 Database Console: http://localhost:8080/h2-console
```

## 🏗️ Architecture

### Spring Boot Components
- **Models**: JPA entities with OOP principles
- **Controllers**: REST API endpoints
- **Services**: Business logic abstraction layer
- **Frontend**: Responsive HTML/CSS/JavaScript interface

### Key Features
- ✅ Full CRUD operations for users and medications
- ✅ Real-time medication adherence tracking
- ✅ Polymorphic notification system
- ✅ Interactive reports and analytics
- ✅ Mobile-responsive design
- ✅ REST API with JSON responses

## 📊 OOP Demonstrations

### Encapsulation Example
```java
public class User {
    private String email;  // Private field
    
    public void setEmail(String email) {
        if (email != null && email.contains("@")) {
            this.email = email.trim();
        } else {
            throw new IllegalArgumentException("Invalid email");
        }
    }
}
```

### Inheritance Example
```java
public abstract class HealthRecord {
    protected Long id;
    public abstract String getSummary();
}

public class MedicationRecord extends HealthRecord {
    @Override
    public String getSummary() {
        return "Medication: " + medicationName;
    }
}
```

### Polymorphism Example
```java
// Same interface, different implementations
List<Notification> notifications = Arrays.asList(
    new MedicationNotification(),
    new AppointmentNotification()
);

for (Notification notif : notifications) {
    notif.send(); // Different behavior for each type
}
```

### Abstraction Example
```java
@Service
public class UserService {
    // Complex logic hidden behind simple interface
    public double calculateOverallAdherence(User user) {
        // Complex calculation abstracted away
        return user.getMedications().stream()
            .mapToDouble(Medication::getAdherenceRate)
            .average().orElse(0.0);
    }
}
```

## 🔧 API Endpoints

### Users
- `GET /api/users` - Get all users
- `GET /api/users/{id}` - Get user by ID
- `GET /api/users/{id}/adherence` - Get adherence summary

### Medications  
- `GET /api/medications/user/{userId}` - Get user medications
- `POST /api/medications/{id}/take` - Mark medication taken
- `GET /api/medications/user/{userId}/schedule` - Get schedule

### Notifications
- `GET /api/notifications/user/{userId}` - Get notifications
- `POST /api/notifications/{id}/send` - Send notification
- `POST /api/notifications/user/{userId}/send-due` - Send due notifications

## 📱 Frontend Features

- **Patient Dashboard**: User info, medications, schedule
- **Medication Tracking**: Real-time adherence monitoring  
- **Notification Center**: Pending and sent notifications
- **Reports**: Summary, adherence, medication, and notification reports
- **Polymorphism Demo**: Interactive OOP concept demonstration

## 🗃️ Sample Data

The application includes sample data for immediate testing:

### Users
- Sarah Johnson (Diabetes & Hypertension)
- Michael Chen (Asthma & High Cholesterol)
- Emily Rodriguez (Rheumatoid Arthritis)

### Medications
- Metformin, Lisinopril, Aspirin, Albuterol, Atorvastatin

### Notifications
- Medication reminders, appointment alerts, adherence warnings

## 📚 Documentation

- **Complete Documentation**: `ChroniCare_SpringBoot_Documentation.pdf`
- **Code Examples**: Detailed OOP implementations with explanations
- **API Reference**: All endpoints with request/response examples
- **Architecture Guide**: Spring Boot structure and design patterns

## 🎓 Educational Value

Perfect for learning:
- Java OOP principles in practice
- Spring Boot framework fundamentals
- REST API design and implementation
- Full-stack web development
- Healthcare software concepts
- Enterprise application patterns

## 🔧 Configuration

### Database
- **Type**: H2 In-Memory Database
- **Console**: http://localhost:8080/h2-console
- **JDBC URL**: jdbc:h2:mem:chronicare
- **Username**: sa (no password)

### Application Properties
- **Port**: 8080
- **Profile**: Development
- **Logging**: DEBUG level for application classes
- **CORS**: Enabled for all origins

## 🚀 Deployment Ready

The application is ready for deployment to:
- Heroku
- Railway
- Google Cloud Platform
- AWS Elastic Beanstalk
- Any Java hosting platform

## 📄 License

Educational project demonstrating Java OOP principles with Spring Boot framework.

---

**Built with ❤️ using Java Spring Boot to demonstrate Object-Oriented Programming excellence**

