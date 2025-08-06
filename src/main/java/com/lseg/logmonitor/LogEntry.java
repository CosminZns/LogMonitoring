package com.lseg.logmonitor;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public record LogEntry(LocalTime time, String description, Status status, String pid) {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final String DELIMITER = ",";
    private static final int LOG_ENTRY_REQUIRED_LENGTH = 4;
    private static final String NUMERIC_REGEX = "\\d+";

    public static LogEntry fromCsvLine(String csvLine) {
        String[] parts = csvLine.split(DELIMITER);

        // Ensure there are exactly 4 parts
        if (parts.length != LOG_ENTRY_REQUIRED_LENGTH) {
            throw new IllegalArgumentException("Log entry must have exactly 4 parts: [time, description, status, pid]");
        }

        LocalTime time = parseTime(parts[0]);
        String description = parseDescription(parts[1]);
        Status status = Status.fromString(parts[2]);
        String pid = parsePid(parts[3]);

        return new LogEntry(time, description, status, pid);
    }

    private static LocalTime parseTime(String part) {
        try {
            return LocalTime.parse(part, FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid time format. Expected HH:mm:ss.");
        }
    }

    private static String parseDescription(String part) {
        if (part == null || part.isBlank()) {
            throw new IllegalArgumentException("Description must not be empty.");
        }
        return part.strip(); // Clean up whitespace
    }

    private static String parsePid(String part) {
        if (!part.matches(NUMERIC_REGEX)) { // Ensure PID is numeric
            throw new IllegalArgumentException("PID must be a numeric value.");
        }
        return part;
    }
}
