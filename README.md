# cafheg-app: Family Allowance Management System (Bachelor exercise)
_The main focus on this project bachelor exercise was about learning to work with a legacy code base. Our main goal was to get hands-on experience with looking at, understanding, and improving the existing application with a series of exercise, following a TDD approach._

`cafheg-app` is a Java-based application designed to manage family allowances. It provides services for determining allowance eligibility, managing recipient information, tracking payments, and generating reports. The application features a RESTful API for interaction and leverages a database for data persistence.

## Core Functionalities

* **Recipient Management:** Allows for finding, creating, updating, and deleting allowance recipients (referred to as "allocataires").
* **Allowance Eligibility:** Implements complex business logic to determine which parent is entitled to receive family allowances based on various criteria (e.g., lucrative activity, parental authority, residence, income).
* **Payment Tracking:** (Implicit) The system handles data related to payments ("versements"), which is crucial for features like preventing deletion of recipients with existing payments.
* **Financial Calculations:** Provides capabilities to sum up total allocations and birth allowances for specific years.
* **PDF Reporting:** Generates PDF documents for recipient-specific allocations and payment summaries.
* **RESTful API:** Exposes its functionalities through a comprehensive REST API.
* **Database Persistence:** Utilizes an H2 in-memory database, managed with Flyway for schema migrations.

## Technologies Used

* **Programming Language:** Java version 11
* **Framework:** Spring Boot (for application structure, REST controllers, and dependency management)
* **Database:** H2 Database Engine
* **Database Migrations:** Flyway
* **Connection Pooling:** HikariCP
* **API:** RESTful APIs (Spring MVC)
* **Testing:**
    * JUnit 5 (for unit and integration testing)
    * Mockito (for mocking dependencies in unit tests)
    * DBUnit (for database state management in integration tests)
    * AssertJ (for fluent assertions in tests)
* **Logging:** SLF4J with Logback
* **PDF Generation:** Apache PDFBox

## Project Setup & Launch

The application is built using Spring Boot.
1.  Ensure you have Java and have correctly set up the dependencies in `/lib`.
2.  The application can be started by running the `main` method in the `ch.hearc.cafheg.infrastructure.application.Application` class.
3.  Upon startup, the H2 database is initialized, and Flyway automatically applies any pending database migrations (`src/main/resources/db/ddl` and `src/main/resources/db/dml`).

## Summary of Implemented Exercises

This project was developed through a series of exercises, each focusing on different aspects of software development:

### Exercise 1: Core Logic Implementation and Refactoring

* **Objective:** Develop and refine the core business logic for determining parental allowance rights and establish good testing practices.
* **Key Achievements:**
    * Implemented the initial logic for `AllocationService#getParentDroitAllocation` to determine which parent is eligible for allowances.
    * Developed a comprehensive test suite for this service, achieving 100% test coverage to ensure reliability.
    * Refactored `AllocationService#getParentDroitAllocation` using Test-Driven Development (TDD). The method was improved to accept a structured `ParentAllocRequest` object instead of a generic `Map<String, Object>`, enhancing type safety and maintainability.
    * Further modified the `getParentDroitAllocation` method according to a detailed decision tree, which considers factors like parents' lucrative activity, parental authority, living arrangements, canton of work, employment status (salaried/independent), and income.
    * Ensured that the public REST API (`/droits/quel-parent`) remained functional throughout the refactoring process.

### Exercise 2: Recipient (Allocataire) Management

* **Objective:** Implement services for managing allowance recipients, including creation, modification, and deletion, with specific business rules.
* **Key Achievements:**
    * **Deletion Service:** Created a service to delete an "allocataire". A key constraint was implemented: an allocataire cannot be deleted if they have associated payments ("versements").  This is handled in `AllocationService#deleteAllocataire`.
    * **Modification Service:** Developed a service to update the name and surname of an "allocataire".  Business rules enforced:
        * The AVS number cannot be modified.
        * The update operation is only performed if the name or surname has actually changed.  This is handled in `AllocationService#updateAllocataire`.
    * **REST API Exposure:** Exposed these recipient management functionalities via REST API endpoints:
        * `DELETE /allocataires/{allocataireId}`
        * `PUT /allocataires/{allocataireId}`

### Exercise 4: Advanced Logging Implementation

* **Objective:** Replace standard console outputs with a robust logging framework using SLF4J and Logback, and configure various logging levels and appenders.
* **Key Achievements:**
    * Integrated SLF4J as the logging facade with Logback as the underlying implementation across the application (e.g., in services, mappers, and controllers).
    * Replaced all `System.out.println` and `printStackTrace` calls with logger invocations.
    * Configured specific logging behaviors in `logback.xml`:
        * **Error Log:** Errors from any package starting with `ch` are logged to `logs/err.log`.
        * **Service Info Log:** Informational messages from the service layer (e.g., `ch.hearc.cafheg.business.*`) are logged to a daily rolling file named `logs/cafheg_{yyyy-MM-dd}.log`.
        * **Console Debug Log:** Debug level messages (and higher) from all packages are logged to the console for development purposes.
    * Ensured that exceptions are logged with their causes.

### Exercise 5: Integration Testing with DBUnit

* **Objective:** Set up a dedicated environment for integration tests and implement tests for database-dependent operations, particularly for the recipient management features from Exercise 2.
* **Key Achievements:**
    * **Project Structure:** Reorganized the project to include a separate directory for integration tests: `src/integration-test/java` and `src/integration-test/resources`.  This ensures a clear separation between unit and integration tests.
    * **DBUnit Configuration:** Integrated DBUnit into the project to manage database state for testing. This involved adding necessary dependencies and configuring a `JdbcDatabaseTester`.
    * **Dataset Creation:** Created an XML dataset (`src/integration-test/resources/dataset.xml`) to define the initial state of the database tables (ALLOCATAIRES, VERSEMENTS) before running integration tests.
    * **Integration Tests:**
        * Implemented `AllocataireMapperIT.java`, which contains integration tests for the allocataire deletion and modification functionalities developed in Exercise 2.
        * These tests use DBUnit to set up the database with the predefined dataset, execute the service methods (`AllocationService#deleteAllocataire` and `AllocationService#updateAllocataire`), and then assert the expected state of the database or the returned objects.
        * A simple placeholder integration test `MyTestsIT.java` was also created as part of the setup process asked by the exercise.

## API Endpoints Overview

The application exposes the following REST API endpoints (details can be found in `RESTController.java`:

* `POST /droits/quel-parent`: Determines which parent is entitled to the allowance based on the provided JSON request body.
* `GET /allocataires`: Retrieves a list of all allowance recipients. Can be filtered by names starting with a specific string (e.g., `?startsWith=Gei`).
* `DELETE /allocataires/{allocataireId}`: Deletes the recipient with the specified ID.
* `PUT /allocataires/{allocataireId}`: Updates the name and surname of the recipient with the specified ID. Expects a JSON body with `lastname` and `firstname`.
* `GET /allocations`: Retrieves a list of all current allocations.
* `GET /allocations/{year}/somme`: Calculates and returns the sum of all allocations for the specified year.
* `GET /allocations-naissances/{year}/somme`: Calculates and returns the sum of all birth allowances for the specified year.
* `GET /allocataires/{allocataireId}/allocations`: Generates and returns a PDF report of allocations for the specified recipient.
* `GET /allocataires/{allocataireId}/versements`: Generates and returns a PDF report of payments (versements) for the specified recipient.

## Future Enhancements (Ideas for Evolution)

The `IDEES_EVOLUTION.txt` file outlines several potential areas for future development:

* **Transaction Management:** Implement proper ACID transaction management (currently relies on auto-commit).
* **Data Access Layer:** Integrate a more advanced SQL library (e.g., Spring JDBC Template, jOOQ, JDBI3) or an ORM framework (e.g., JPA/Hibernate, MyBatis).
* **Dependency Injection:** Fully leverage Spring Core for comprehensive dependency injection.
* **User Interface:**
    * Develop a server-side rendered front-end (e.g., using Spring MVC with Thymeleaf).
    * Alternatively, create a client-side front-end using a modern JavaScript framework (e.g., VueJS).
* **Security:** Implement application security using a framework like Spring Security.
* **Expanded Services:** Offer additional web services to extend the application's capabilities.