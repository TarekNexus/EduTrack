# EduTrack

A Student Management System with a Java Swing desktop UI (light, professional
blue theme via [FlatLaf](https://www.formdev.com/flatlaf/)) backed by a
[Neon](https://neon.tech) serverless Postgres database.

Developed by **[Md. Tarek](https://tarekdev59.vercel.app/)**.

## Features

- **Login** — admin credentials stored in the database (not hardcoded), with
  a default account seeded automatically on first run.
- **Dashboard** — live stats (total / male / female students, department
  count) and a "recently added students" table.
- **Add / Edit Student** — student ID, name, email, phone, address, gender,
  department, semester, CGPA, blood group, and a photo upload with a live
  circular preview.
- **View Students** — searchable table (by ID, name, or department) with a
  photo thumbnail per row and inline View / Edit / Delete actions.
- **Student Details** — a popup with the full record and photo, opened by
  double-clicking a row.
- **Department management** — add, rename, or delete departments; deleting a
  department unassigns its students rather than blocking the delete.
- **About** — app info and developer credit.

The app auto-creates its schema (`students`, `departments`, `admins`) on
first connection — no manual SQL required.

## 1. Create your Neon database

1. Sign up / log in at [neon.tech](https://neon.tech) and create a project.
2. Open **Connection Details** in the Neon console and copy the connection
   string. It looks like:
   ```
   postgresql://<user>:<password>@<endpoint>.neon.tech/<dbname>?sslmode=require
   ```

## 2. Configure the app

Copy the example config and paste in your connection string:

```powershell
copy src\main\resources\config.properties.example config.properties
```

Edit `config.properties` (in the project root) and set `db.url` to the string
from step 1. This file is git-ignored, so your credentials never get committed.

Alternatively, skip the file entirely and set an environment variable instead:

```powershell
$env:DB_URL = "postgresql://user:password@endpoint.neon.tech/dbname?sslmode=require"
```

## 3. Run it

No local Maven install required — this project ships the Maven Wrapper, which
downloads its own Maven distribution on first use.

```powershell
.\mvnw.cmd clean compile exec:java
```

Or build a self-contained runnable jar and launch it directly:

```powershell
.\mvnw.cmd clean package
java -jar target\studenthub.jar
```

On first launch the app connects to Neon, creates the schema if it doesn't
exist yet, and seeds a default admin login:

```
Username: admin
Password: admin123
```

## Project layout

```
src/main/java/
  Main.java                        entry point: theme + connect + seed admin + launch Login
  database/DBConnection.java       resolves DB settings, connection reuse, schema bootstrap
  model/                           Student, Department, Admin, DashboardStats
  dao/                             StudentDAO, DepartmentDAO, AdminDAO
  ui/
    Login.java                     split-panel login screen (brand panel + form)
    Dashboard.java                 app shell: top bar, sidebar, dashboard home stats
    AddStudent.java                add/edit student form (sectioned, with photo)
    ViewStudents.java              searchable student table with photo + row actions
    StudentDetails.java            student details popup
    DepartmentManagement.java      department CRUD
    About.java                     about screen + developer credit
    Theme.java                     colors, fonts, FlatLaf setup
    GradientPanel.java, RoundedPanel.java, PillButton.java, NavButton.java, StatCard.java
    StudentTableModel.java         table model backing the student list
  utils/
    Validation.java                field validation helpers
    ImageHelper.java               photo copy, circular-crop, and placeholder-avatar generation
database.sql                       reference schema (optional manual setup via Neon's SQL editor)
```

Student photos are copied into `%USERPROFILE%\.edutrack\images\` (not a path
relative to the working directory), so they keep resolving correctly no
matter how you launch the app next time.

## Notes on the implementation

- **Single reused connection.** `DBConnection` keeps one JDBC connection open
  for the app's lifetime instead of reconnecting to Neon on every click —
  opening a fresh connection per query made the UI feel slow. It transparently
  reconnects if Neon's serverless compute suspends the connection after being
  idle.
- **Background writes.** Login, saving a student, and department CRUD all run
  on a `SwingWorker` with a disabled-button busy state ("Signing in…",
  "Saving…") so the UI never freezes, plus a success confirmation afterward.
- **No emoji/pictograph glyphs.** Icons are either plain text or drawn with
  `Graphics2D` (see `StatCard`'s accent dot, `Login`'s bullet dots, and the
  initials-avatar placeholder) since color emoji don't reliably render in
  this Swing/font setup.
- **Password hashing** is SHA-256 with a static pepper (see `AdminDAO`) —
  simple and adequate for a school project, not meant for production use.

## Troubleshooting

- **"No database configuration found"** — you haven't created `config.properties`
  or set `DB_URL` yet (step 2).
- **Connection timeout** — Neon's compute auto-suspends when idle; the first
  query after idling can take a few extra seconds to wake up, which is normal.
- **SSL error** — make sure `sslmode=require` is present in your connection string.
- **Invalid username or password on login** — the default admin is `admin` /
  `admin123`, seeded automatically the first time the app connects.
- **A student's photo shows the placeholder instead of the real picture** —
  it was likely uploaded before the photo-path fix (photos are now stored
  under `%USERPROFILE%\.edutrack\images\`); re-upload it from the Edit form.
