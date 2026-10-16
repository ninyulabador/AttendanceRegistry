# Attendance Registry System

A desktop attendance registry built with Java Swing. The application supports account registration and login, student profile views, attendance time-in/time-out, searchable admin records, and a recycle bin for deleted student records.

## Requirements

- Java Development Kit (JDK) installed and available on your `PATH`
- Windows PowerShell (for the included `run.ps1` helper)

Check that Java is available:

```powershell
java -version
javac -version
```

## Run the application

From the repository root, run:

```powershell
.\run.ps1
```

Or compile and launch manually:

```powershell
javac -d out src\main\java\registry\AttendanceRegistryApp.java src\main\java\registry\AttendanceSystemFlow.java
java -cp out registry.AttendanceRegistryApp
```

The compiled classes are written to `out/`, which is ignored by Git.

## Features

- Register an account and log in with its credentials.
- Student accounts can view their own profile and attendance status, including time in and time out.
- The built-in admin account can view active records, search records, record a selected student's time in/time out using the current time, and move selected records to the recycle bin.
- Deleted student records can be viewed and restored.
- Application data is stored locally in tab-separated files under `data/`.

## Local data files

- `data/students.tsv` — registered student accounts and profile data
- `data/attendance_records.tsv` — attendance records
- `data/deleted_students.tsv` — records moved to the recycle bin

Back up these files before making manual changes or replacing the application. Do not commit real student information, passwords, or other private data to a public repository.

## Admin account and security

The current application includes a built-in admin login (`admin` / `admin123`). This is a default credential, not a secure production configuration. Change or remove it before using the application with real student data, and do not reuse this password elsewhere.

## Troubleshooting

- If `java` or `javac` is not recognized, install a JDK and add its `bin` directory to your system `PATH`.
- If compilation fails, confirm both Java source files are present under `src/main/java/registry/` and review the compiler output.
- If records do not appear as expected, confirm the application is launched from the repository root so the relative `data/` paths resolve correctly.
