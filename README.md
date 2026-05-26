# Attendance Registry System

A Java Swing project based on the sketched registry flow: login, create account, dashboard/profile, searchable record table, delete, and logout.

## Run

From this folder:

```powershell
javac -d out src\main\java\registry\AttendanceRegistryApp.java
java -cp out registry.AttendanceRegistryApp
```

Or use:

```powershell
.\run.ps1
```

## Notes

- Register an account first with a username and password.
- Login only works with a registered username and password.
- Student users can only view their own profile, attendance status, time in, and time out.
- Admin login is built in:
  - Username: `admin`
  - Password: `admin123`
- Only the admin can manage the system: view all active records, search records, record a selected student's time in/time out with the current time, and move selected records to the recycle bin.
- Deleted students can be viewed in the recycle bin and restored back to the main attendance system.
- Active records are saved in `data/students.tsv`; deleted records are saved in `data/deleted_students.tsv`.
