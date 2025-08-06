# Log Monitor Tool

A simple Java-based log monitoring tool designed to analyze job durations and generate reports based on thresholds. The tool reads log entries from a file, calculates job durations, and logs warnings or errors for tasks that exceed time thresholds.

---

## Features

- Reads log entries from a specified file in CSV-like format.
    - Example log format: `HH:mm:ss,<description>,<status>,<PID>`
- Calculates job durations using `START` and `END` log entries.
- Logs:
    - **Warnings** if job duration exceeds 5 minutes.
    - **Errors** if job duration exceeds 10 minutes.
- Handles missing log entries
- Logs error messages in case of issues (e.g., log file not found).

---

## Technology Stack

- **Java 21** or later
- **SLF4J** for logging
- **JUnit 5** and **Mockito** for unit testing
- **Maven** for building and dependency management

---