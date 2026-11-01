# Bank Transfer System

A Spring Boot web application for managing bank accounts and performing secure money transfers. This project was developed for **COSC 32133 / BECS 32263 – Full-Stack Software Development, Assignment 01** at the University of Kelaniya.

## Technologies Used

- Java 21
- Spring Boot
- Spring MVC
- Spring Data JPA
- Hibernate
- MySQL / MariaDB
- Thymeleaf
- Lombok
- Maven
- HTML and CSS
- Git and GitHub

## Main Features

- View all customer accounts and balances.
- Create new customer accounts.
- Search customers by name.
- Transfer money between accounts.
- Use `@Transactional` to ensure atomic transfers.
- Roll back transactions when failures occur.
- Simulate system failures to demonstrate rollback.
- Record failed transactions using `REQUIRES_NEW`.
- View transaction history for each account.
- Display the latest successful and failed transactions.
- Search for large transfers using custom `@Query`.
- Calculate the total successful amount sent by an account.
- Deposit and withdraw money.
- Apply a 1% transfer fee and credit it to bank account `999999`.

## Project Structure

```text
bank-transfer-app/
├── src/
│   ├── main/
│   │   ├── java/com/example/bankapp/
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── exception/
│   │   │   ├── model/
│   │   │   ├── repository/
│   │   │   └── service/
│   │   └── resources/
│   │       ├── templates/
│   │       └── application.properties
│   └── test/
├── pom.xml
├── mvnw
├── mvnw.cmd
└── README.md
```

## Database Configuration

Create a MySQL database named:

```sql
CREATE DATABASE bank_db;
```

Configure the database connection in `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/bank_db
spring.datasource.username=root
spring.datasource.password=

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

Update the username, password and port according to your local MySQL configuration.

## How to Run the Application

### Prerequisites

- JDK 21 or later
- MySQL or XAMPP
- Git
- Visual Studio Code

### Steps

1. Clone the repository:

```bash
git clone https://github.com/menakadissanayake/bank-transfer-app.git
```

2. Open the project folder in VS Code.

3. Start MySQL using XAMPP.

4. Configure the database connection.

5. Run the application using the Maven Wrapper:

**Windows:**

```powershell
.\mvnw.cmd spring-boot:run
```

6. Open the application in your browser:

```text
http://localhost:8080
```

## Application URLs

| Method | URL | Description |
|---|---|---|
| GET | `/` | Dashboard |
| POST | `/customers` | Add a customer |
| POST | `/transfer` | Transfer money |
| GET | `/accounts/{accountNumber}/history` | View account history |
| GET | `/large-transfers?min=10000` | View large transfers |
| POST | `/deposit` | Deposit money |
| POST | `/withdraw` | Withdraw money |

## Transaction Management

The application uses Spring's `@Transactional` annotation to maintain database consistency.

- **Atomicity:** All transfer updates succeed or roll back together.
- **Consistency:** Account balances remain consistent.
- **Isolation:** Database transactions are managed by Spring and the database.
- **Durability:** Committed transactions are permanently stored.

Failed transactions are recorded separately using `Propagation.REQUIRES_NEW`.

## Custom Repository Queries

The application uses Spring Data JPA `@Query` for:

- Finding customers by account number.
- Counting customer accounts and emails.
- Searching customers by name.
- Sorting customers by name.
- Calculating total account balances.
- Counting customers using native SQL.
- Debiting and crediting customer accounts.
- Finding transaction histories.
- Finding large transfers.
- Calculating the total amount sent by an account.

## GitHub Repository

https://github.com/menakadissanayake/bank-transfer-app

