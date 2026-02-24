@echo off
echo Fixing Gradle build issues...

echo Stopping any running Gradle processes...
taskkill /F /IM java.exe 2>NUL
taskkill /F /IM gradle.exe 2>NUL

echo Cleaning Gradle cache...
if exist "C:\Users\urooj\.gradle\caches" (
    rmdir /S /Q "C:\Users\urooj\.gradle\caches"
    echo Cache cleaned successfully.
) else (
    echo Cache directory not found.
)

echo Running Gradle build...
cd /d "c:\Users\urooj\CascadeProjects\windsurf-project"
gradlew.bat build --info

echo Build process completed.
pause
