# Course Registration & Student Management (Servlets)

## Overview

Simple Java Servlet/JSP webapp (no frameworks) running on Tomcat. It covers:

- Course registration flow (register.html → RegisterServlet → ConfirmationServlet)
- Student CRUD with JDBC + session guard (LoginServlet + StudentServlet)
- JSP view with included header (MessageServlet)

## Tech Stack

- Java Servlets/JSP (configured in web.xml, no annotations)
- Tomcat 7+ (tested with tomcat7-maven-plugin)
- JDBC (PostgreSQL)
- Maven WAR build

## Prerequisites

- JDK 8+
- Maven 3+
- Tomcat 7+ (or use `mvn tomcat7:run`)
- PostgreSQL database (Neon-compatible)

## Configuration (.env)

Create `.env` in project root (or in `catalina.base`) based on `.env.example`:

```
DB_URL=jdbc:postgresql://HOST:PORT/DB?sslmode=require
DB_USER=...
DB_PASSWORD=...
# Optional: PGHOST, PGPORT, PGDATABASE, PGUSER, PGPASSWORD, PGSSLMODE
```

## Database Setup

Create the student table:

```sql
CREATE TABLE IF NOT EXISTS student (
	id INT PRIMARY KEY,
	name VARCHAR(255) NOT NULL,
	department VARCHAR(255) NOT NULL
);
```

## Install Dependecies

```bash
mvn install
```

## Build

```bash
mvn clean package
```

Resulting WAR: `target/course-registration-management.war`.

## Run (dev)

```bash
mvn tomcat7:run
```

App served at http://localhost:8080/

## Key Endpoints & Flow

- `/register` (POST): register student; forwards to `/confirmation` showing entered data + university name from context-param.
- `/confirmation`: renders confirmation HTML.
- `/login` (POST): simple login that seeds `HttpSession user` attribute.
- `/student` (POST): CRUD actions via `action` param (`create|read|update|delete`); forwards to JSP.
- `studentResult.jsp`: renders forms and student table; includes `/message` header.

## Notes

- All servlets mapped in `src/main/webapp/WEB-INF/web.xml` (no annotations).
- DB config is read from environment/.env; if missing, app fails fast on startup.
- MessageServlet must not close the response writer (fixed).

## Troubleshooting

- Missing DB config: ensure `.env` is loaded (project root or `catalina.base`).
- JDBC driver: PostgreSQL driver is bundled; for MySQL switch DB_URL/driver accordingly.
- JSP stream closed errors: ensure MessageServlet does not close the writer (already handled).
