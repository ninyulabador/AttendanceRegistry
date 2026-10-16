# Compile all application source files before launching.
$ErrorActionPreference = "Stop"

$sourceFiles = @(
    "src\main\java\registry\AttendanceRegistryApp.java",
    "src\main\java\registry\AttendanceSystemFlow.java"
)

New-Item -ItemType Directory -Force -Path "out" | Out-Null
javac -d out $sourceFiles

if ($LASTEXITCODE -ne 0) {
    throw "Java compilation failed with exit code $LASTEXITCODE."
}

java -cp out registry.AttendanceRegistryApp
if ($LASTEXITCODE -ne 0) {
    throw "Application exited with code $LASTEXITCODE."
}
