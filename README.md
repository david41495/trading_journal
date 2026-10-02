# Trading Journal

A full-stack trading journal for recording, reviewing, and organizing stock, option, and futures trades. The application combines a Spring Boot backend with a responsive browser interface, secure user accounts, and user-specific trade data.

This project was built as a practical portfolio application and demonstrates backend API development, relational data persistence, authentication, authorization, validation, automated testing, and frontend integration.

## Features

- Create an account and sign in securely
- Record stock, option, and futures trades
- Track long and short positions
- Manage planned, open, closed, and cancelled trades
- Store entry and exit prices, quantity, fees, stop loss, profit target, setup, timeframe, and notes
- Calculate realized profit and loss
- Review dashboard statistics, including total trades, win rate, net P&L, and open trades
- Filter trade history by asset type
- Delete trades from the journal
- Save personal trading commandments in the browser
- Keep each user's trades private and separate from other accounts

## Technology Stack

### Backend

- Java 21
- Spring Boot 4
- Spring Web MVC
- Spring Data JPA
- Spring Security
- Jakarta Bean Validation
- Maven

### Data

- MySQL for local application data
- H2 for automated tests
- Hibernate ORM

### Frontend

- HTML5
- CSS3
- Vanilla JavaScript
- Fetch API

### Testing

- JUnit
- Spring Boot Test
- Spring Security Test
- MockMvc integration tests

## Application Design

The browser interface communicates with REST endpoints provided by Spring Boot. Spring Security manages session-based authentication and CSRF protection. The service and repository layers scope trade operations to the authenticated user so one account cannot access another account's journal entries.

```text
Browser UI
   |
   | HTTP / JSON
   v
Spring MVC Controllers
   |
   v
Service Layer
   |
   v
Spring Data JPA Repositories
   |
   v
MySQL Database
```

Passwords are never stored as plain text. They are hashed with BCrypt before being saved.

## Getting Started

### Prerequisites

- Java 21 or newer
- MySQL 8 or newer
- Git

Maven does not need to be installed separately because the repository includes the Maven wrapper.

### 1. Clone the repository

```bash
git clone https://github.com/david41495/trading_journal.git
cd trading_journal
```

### 2. Create the MySQL database and application user

Run the following statements in MySQL. Replace `choose-a-password` with your own local password.

```sql
CREATE DATABASE trade_journal;
CREATE USER 'tradejournal_app'@'localhost' IDENTIFIED BY 'choose-a-password';
GRANT ALL PRIVILEGES ON trade_journal.* TO 'tradejournal_app'@'localhost';
FLUSH PRIVILEGES;
```

### 3. Configure the database password

Create a file named `local.properties` in the project root:

```properties
spring.datasource.password=choose-a-password
```

The `local.properties` file is excluded from Git so credentials are not uploaded to the repository.

The application can also be configured with environment variables:

| Variable | Purpose | Default |
| --- | --- | --- |
| `DB_URL` | JDBC connection URL | Local `trade_journal` MySQL database |
| `DB_USERNAME` | Database username | `tradejournal_app` |
| `DB_PASSWORD` | Database password | Empty |
| `PORT` | Application port | `8080` |

### 4. Run the application

On macOS or Linux:

```bash
./mvnw spring-boot:run
```

On Windows:

```powershell
mvnw.cmd spring-boot:run
```

Open [http://localhost:8080](http://localhost:8080) and create an account. After signing in, you can begin adding trades to the journal.

## Running the Tests

On macOS or Linux:

```bash
./mvnw test
```

On Windows:

```powershell
mvnw.cmd test
```

The test suite covers application startup, authentication behavior, secured trade access, and service-level ownership rules.

## API Overview

Most API routes require an authenticated session.

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `POST` | `/api/auth/register` | Create an account |
| `POST` | `/api/auth/login` | Sign in |
| `POST` | `/api/auth/logout` | Sign out |
| `GET` | `/api/auth/me` | Get the signed-in user |
| `GET` | `/api/auth/csrf` | Obtain a CSRF token |
| `GET` | `/api/trades` | List the user's trades |
| `GET` | `/api/trades/{id}` | Get one trade |
| `POST` | `/api/trades` | Create a trade |
| `PUT` | `/api/trades/{id}` | Update a trade |
| `DELETE` | `/api/trades/{id}` | Delete a trade |

Additional search endpoints support filtering by symbol, date, asset type, direction, and trade status.

## Project Structure

```text
src/
├── main/
│   ├── java/          Spring Boot application code
│   └── resources/
│       ├── static/    HTML, CSS, and JavaScript
│       └── application.properties
└── test/              Unit and integration tests
```

## Engineering Highlights

- Trade queries are scoped to the authenticated user at the repository and service layers.
- Validation rejects incomplete or invalid trade submissions before persistence.
- Monetary calculations use `BigDecimal` to avoid floating-point rounding errors.
- Session authentication and CSRF protection secure state-changing requests.
- Application secrets remain outside version control through local properties or environment variables.
- H2 provides isolated automated tests without requiring the development database.

## Roadmap

- Add richer performance analytics and charts
- Add an in-page trade editing workflow
- Add screenshot and chart attachments to journal entries
- Introduce database migrations with Flyway
- Add email verification and password recovery
- Package the application with Docker
- Deploy a live portfolio demonstration

## Author

David Serrano — [GitHub](https://github.com/david41495)

