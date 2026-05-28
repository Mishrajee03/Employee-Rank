# EmployeeRank — Full-Stack Employee Scoring Platform

> **Score employees like students. Rank monthly. Hire by credits.**

A production-ready Spring Boot + Vanilla JS application where employees receive structured scores across categories, get monthly report cards with grades (A+ to F), accumulate credits, build public profiles, and companies can hire directly based on verified credit scores.

---

## 🏗 Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 17, Spring Boot 3.2, Spring Security (JWT) |
| Database | MySQL 8 (H2 for tests) |
| ORM | Spring Data JPA / Hibernate |
| Frontend | HTML5, CSS3 (vanilla), Vanilla JS (ES6+) |
| Charts | Chart.js 4 |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Build | Maven |

---

## 📁 Project Structure

```
employeerank/
├── src/main/java/com/employeerank/
│   ├── EmployeeRankApplication.java
│   ├── config/
│   │   ├── SecurityConfig.java        ← JWT security, CORS, route rules
│   │   ├── OpenApiConfig.java         ← Swagger setup
│   │   └── DataSeeder.java            ← Seeds admin + demo data on startup
│   ├── controller/
│   │   ├── PageController.java        ← Serves HTML pages (Thymeleaf)
│   │   ├── AuthController.java        ← /api/auth/**
│   │   ├── UserController.java        ← /api/users/**, /api/leaderboard/**, /api/public/**
│   │   ├── ScoreController.java       ← /api/scores/**
│   │   ├── MonthlyResultController.java ← /api/results/**
│   │   ├── CompanyController.java     ← /api/companies/**
│   │   └── JobController.java         ← /api/jobs/**
│   ├── entity/
│   │   ├── User.java
│   │   ├── Company.java
│   │   ├── Score.java
│   │   ├── MonthlyResult.java
│   │   ├── Badge.java
│   │   ├── JobPosting.java
│   │   └── JobApplication.java
│   ├── dto/                           ← All request/response DTOs
│   ├── enums/                         ← Role, ScoreCategory, BadgeType
│   ├── exception/                     ← GlobalExceptionHandler + custom exceptions
│   ├── repository/                    ← Spring Data JPA repositories
│   ├── security/                      ← JwtUtils, JwtAuthFilter, UserDetailsService
│   └── service/impl/                  ← Business logic services
│
├── src/main/resources/
│   ├── application.properties         ← Configure DB/JWT/mail here
│   ├── templates/                     ← Thymeleaf HTML pages
│   │   ├── login.html
│   │   ├── dashboard.html
│   │   ├── scores.html
│   │   ├── results.html
│   │   ├── leaderboard.html
│   │   ├── jobs.html
│   │   ├── profile.html
│   │   ├── company.html
│   │   └── public-profile.html
│   └── static/
│       ├── css/
│       │   ├── main.css               ← Design system, sidebar, layout
│       │   ├── auth.css
│       │   ├── dashboard.css
│       │   ├── leaderboard.css
│       │   ├── jobs.css
│       │   └── profile.css
│       └── js/
│           ├── api.js                 ← All API calls + auth helpers
│           ├── auth.js
│           ├── dashboard.js
│           ├── leaderboard.js
│           ├── jobs.js
│           └── profile.js
```

---

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8+
- IntelliJ IDEA (recommended)

### 1. Database Setup
```sql
CREATE DATABASE employeerank_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. Configure `application.properties`
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/employeerank_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
```

### 3. Open in IntelliJ IDEA
1. `File → Open` → select the `employeerank` folder
2. IntelliJ will detect `pom.xml` and auto-import Maven
3. Wait for dependencies to download
4. Run `EmployeeRankApplication.java`

### 4. Access the app
| URL | Description |
|-----|-------------|
| http://localhost:8080/ | Login page |
| http://localhost:8080/dashboard | Dashboard |
| http://localhost:8080/swagger-ui.html | API documentation |

---

## 🔑 Default Credentials (seeded on startup)

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@employeerank.com | Admin@1234 |
| Company | company@demo.com | Company@1234 |
| Manager | manager@demo.com | Manager@1234 |
| Employee | alice@demo.com | Employee@1234 |
| Employee | bob@demo.com | Employee@1234 |

---

## 🎯 Core Features

### Scoring System
- Managers score employees across **10 categories**: Performance, Punctuality, Teamwork, Communication, Leadership, Innovation, Customer Service, Technical Skills, Problem Solving, Adaptability
- Peer reviews supported (employees can review each other)
- Points from 0–100 per category
- Duplicate prevention: one score per category per scorer per month

### Grading & Credits
| Score % | Grade | Credits Earned |
|---------|-------|----------------|
| ≥ 95% | A+ | 100 |
| ≥ 90% | A | 90 |
| ≥ 85% | A- | 80 |
| ≥ 80% | B+ | 70 |
| ≥ 75% | B | 60 |
| ≥ 70% | B- | 50 |
| ≥ 60% | C | 35 |
| ≥ 50% | D | 20 |
| < 50% | F | 10 |

### Monthly Results
- Auto-processed via cron job at end of each month (`0 0 0 L * ?`)
- Company-wide ranking calculated
- Badges awarded to top performers
- Results can be published by managers with comments

### Badges
🥉 Bronze → 🥈 Silver → 🥇 Gold → 💎 Platinum → 💠 Diamond → ⬡ Legend

### Job Board
- Companies post jobs with **minimum credit requirements**
- Employees only see jobs they qualify for
- One-click apply with optional cover letter
- Companies manage applications (Pending / Shortlisted / Accepted / Rejected)

### Public Profiles
- Shareable profile URL: `/public/{username}`
- Shows credits, badges, performance history, skills, social links
- Privacy toggle — employees control visibility

---

## 📡 Key API Endpoints

```
POST   /api/auth/register          Register new user
POST   /api/auth/login             Login (returns JWT)
POST   /api/auth/refresh           Refresh access token
POST   /api/auth/logout            Logout

GET    /api/users/me               Get current user profile
PUT    /api/users/me               Update profile
GET    /api/leaderboard/global     Global leaderboard
GET    /api/public/profile/{user}  Public profile

POST   /api/scores                 Add score (manager/peer)
GET    /api/scores/my-scores       My scores
GET    /api/scores/my-summary      My monthly summary

POST   /api/results/generate/{id}  Generate result for employee
POST   /api/results/{id}/publish   Publish a result
GET    /api/results/my-results     My results history
GET    /api/results/company/{id}/leaderboard  Company monthly rank

GET    /api/companies/search       Search companies
POST   /api/companies              Create company

GET    /api/jobs/eligible          Jobs I can apply for (by credits)
POST   /api/jobs/{id}/apply        Apply for a job
GET    /api/jobs/my-applications   My applications
```

Full Swagger docs: http://localhost:8080/swagger-ui.html

---

## ⚙ Scheduled Jobs

```properties
# Runs last day of every month at midnight
app.scheduling.monthly-result.cron=0 0 0 L * ?
```

To trigger manually via API (admin only):
```
POST /api/results/process?month=5&year=2026
```

---

## 🔒 Security

- **JWT-based stateless authentication** (access + refresh token)
- **Role-based access control** via `@PreAuthorize`
  - `ROLE_EMPLOYEE` — view scores, apply to jobs, peer review
  - `ROLE_MANAGER` — score employees, publish results
  - `ROLE_COMPANY` — create/manage company, post jobs, view applicants
  - `ROLE_ADMIN` — full access including bulk processing
- CORS configured for all origins in dev (restrict in production)
- BCrypt password hashing

---

## 🛠 Production Checklist

- [ ] Set strong `app.jwt.secret` (min 256-bit)
- [ ] Configure real SMTP in `application.properties`
- [ ] Restrict CORS origins in `SecurityConfig.java`
- [ ] Set `spring.jpa.hibernate.ddl-auto=validate` after first run
- [ ] Enable HTTPS
- [ ] Configure file upload path for profile pictures
- [ ] Set `spring.thymeleaf.cache=true`
