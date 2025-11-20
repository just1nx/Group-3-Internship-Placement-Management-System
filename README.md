# Group 3 — Internship Placement Management System

A simple MVC, console‑driven system to manage internships between Students, Company Representatives, and Career Center Staff. Built in Java with persistent CSV storage and browsable Javadoc.

## Features
- Authentication with secure password hashing and role‑based access
- Student: browse/apply/withdraw, view notifications
- Company Representative: post/manage internships, review applications, view notifications
- Career Center Staff: approve representatives, oversee data
- CSV‑based persistence under `data/`
- Generated Javadoc in `Javadoc/`

## Architecture Overview
- `src/app/` — entrypoint (`src/app/InternshipManagementSystem.java`)
- `src/boundary/` — console UIs (e.g., `src/boundary/StudentInterface.java`, `src/boundary/CompanyRepresentativeInterface.java`)
- `src/control/` — controllers (e.g., `src/control/AuthenticationController.java`)
- `src/entity/` — domain models
- `data/` — CSV datasets

Boundary (UI) → Control (logic) → Entity (data).

## Data Persistence
All state is stored in CSV files under `data/` (e.g., students, company representatives, internships, applications, withdrawals, staff). This keeps data across runs and allows easy backup/migration by copying files.

## Notifications
- Student Interface (`src/boundary/StudentInterface.java`)
  - Shows updates like application status changes and withdrawal outcomes, retrieved via `src/control/StudentController.java`.
- Company Representative Interface (`src/boundary/CompanyRepresentativeInterface.java`)
  - Shows new applications, student withdrawals, and related updates, via `src/control/CompanyRepresentativeController.java`.

These are derived from the CSV records (applications, withdrawals, statuses) and marked as viewed after display.

## Password Hashing
Passwords are never stored in plaintext. Each user record stores a non‑reversible, salted, and work‑factored hash. On login, the input password is processed the same way and compared using constant‑time checks. Hashed credentials are kept in the user CSV files under `data/`.

## How to Run

Prerequisites:
- JDK 17+

Using IntelliJ IDEA:
1. Open the project root.
2. Set the project SDK to JDK 17+.
3. Run the main class `src/app/InternshipManagementSystem.java`.

Using terminal (macOS/Linux):
```bash
# from the project root
mkdir -p out
find src -name "*.java" -print0 | xargs -0 javac -d out
java -cp out app.InternshipManagementSystem
