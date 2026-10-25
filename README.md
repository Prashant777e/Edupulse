# EduPulse

EduPulse is a Java Swing-based campus engagement and academic management application designed for students and faculty. It provides a simple interface for registration, login, communication, peer grading, notifications, and faculty availability tracking.

The project is built around a MySQL database and uses JDBC to connect the frontend interface with backend data.

## Overview

EduPulse aims to improve student engagement and communication within an academic environment by offering:

- Student registration and role-based login
- Student dashboard with energy points and leaderboard
- Faculty dashboard for managing students and notifications
- Messaging system between students
- Peer teaching and peer grading mechanism
- Faculty availability management
- Notification history and sending alerts

## Key Features

### Student Features
- Sign up and login with a student account
- View dashboard with student name and energy score
- Track leaderboard rankings based on energy points
- Send and receive messages
- Award peer points to other students for collaboration and support
- View peer teaching history
- Check faculty availability

### Faculty Features
- Faculty login and dashboard
- Manage student records
- Send notifications to students
- View notification history
- Manage faculty availability schedule

### Admin Ready
- The project structure includes support for role-based login and future admin dashboard expansion.

## Tech Stack

- Java
- Swing (GUI)
- MySQL
- JDBC
- NetBeans / IntelliJ / VS Code compatible Java environment

## Project Structure

The repository contains Java files for the user interface and database connection, including:

- `LoginPage.java` – login screen
- `RegistrationPage.java` – student registration form
- `StudentDashboard.java` – student main dashboard
- `FacultyDashboard.java` – faculty dashboard
- `DashboardPanel.java` – student dashboard panel and leaderboard
- `MessagePanel.java` – messaging interface
- `peergrading.java` – peer points and peer teaching system
- `SendNotificationPanel.java` – notification sending panel
- `AvailabilityPanel.java` – faculty availability management
- `NotificationHistoryPanel.java` – notification history panel
- `DatabaseConnection.java` – MySQL connection configuration
- `README.md` – project documentation

## Prerequisites

Before running the project, make sure you have:

- Java JDK 8 or higher
- MySQL Server installed and running
- MySQL Connector/J in your classpath
- A database named `edupulsenew`

## Database Setup

Update the database credentials inside `DatabaseConnection.java` if needed:

```java
private static final String URL = "jdbc:mysql://localhost:3306/edupulsenew";
private static final String USER = "root";
private static final String PASSWORD = "root";
```

Make sure your MySQL server is configured to allow the connection.

## Run the Project

Compile the Java files:

```bash
javac *.java
```

Run the application:

```bash
java userinterface.LoginPage
```

or run the main class from your IDE.

## Database Notes

The application expects tables such as:

- `users`
- `students`
- `faculty`
- `messages`
- `peer_teaching`
- notification and availability-related tables

If your MySQL schema differs, update the SQL queries used in the Java files accordingly.

## Application Concept

EduPulse is designed to bring together the academic, communication, and engagement aspects of student life into one system. It is especially useful for institutions looking for a simple desktop-based platform to manage student interaction and faculty communication without needing a full web application.

## Future Enhancements

Possible improvements for this project include:

- Admin dashboard and role management
- Better UI/UX design
- Email or SMS notifications
- Better security with password hashing
- Reports and analytics
- Web-based version using Java EE, Spring Boot, or React

## License

This project is currently provided as an educational project and may be used for learning and modification.

## Author

Prashant777e
