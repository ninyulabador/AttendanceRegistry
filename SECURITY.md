# Security and Privacy

## Important: review repository data before using real student records

This application stores student and attendance information in local TSV files under `data/`.
Do not commit real student information, attendance history, or passwords to Git.

A `.gitignore` rule prevents new untracked TSV files from being added accidentally, but it
does **not** remove files already tracked by Git or erase older copies from repository history.

## Immediate actions for repository maintainers

1. Restrict repository visibility while assessing whether real student data or credentials were exposed.
2. Treat any password or default credential committed to the repository as compromised. Rotate it anywhere it was reused.
3. Remove real records from the current tracked files after making a secure, access-controlled backup outside the repository.
4. Review repository history and coordinate a history cleanup if sensitive data was committed. A normal file deletion does not erase historical copies.
5. Use synthetic data only in examples and tests.
6. Do not store passwords in plaintext. Implement a password-hashing scheme designed for passwords (for example, PBKDF2 with a unique salt and appropriate work factor) before using this application with real accounts.
7. Restrict student access to the authenticated student's own profile and records; verify administrator authorization for management actions.

## Local data files

The application currently expects local files under `data/`. These files are runtime data, not source code.
Keep them local and out of version control. If example fixtures are needed, use clearly synthetic data
in files named `*.example.tsv`.

## Scope of this change

This document and the ignore rules are initial safeguards, not a complete security fix. They do not
remove already-tracked data, rotate credentials, rewrite Git history, or implement password hashing.
Those steps must be completed and verified before this project is used with real student records.
