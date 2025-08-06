package com.lseg.logmonitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LogMonitor {

    public static Logger logger = LoggerFactory.getLogger(LogMonitor.class);
    public static final String FILE_PATH = "src/main/resources/logs.log";
    private static final int WARNING_LOG_THRESHOLD = 300;  //5 minutes in seconds
    private static final int ERROR_LOG_THRESHOLD = 600;    //10 minutes in seconds

    public void generateOutput() {

        try (var lines = Files.lines(Path.of(FILE_PATH))) {
            List<LogEntry> logEntries = lines
                    .map(LogEntry::fromCsvLine)
                    .toList();

            Map<String, Tuple> groupedByPid = logEntries.stream()
                    .collect(Collectors.toMap(
                            LogEntry::pid,      // Group by PID
                            LogMonitor.Tuple::create, // Create a Tuple for each log entry
                            LogMonitor.Tuple::merge  // Merge tuples as necessary
                    ));

            checkLogDuration(groupedByPid);
        } catch (Exception e) {
            logger.error("Error reading the log file. File path: {}. Error: {}", FILE_PATH, e.getMessage(), e);
        }
    }


    private void checkLogDuration(Map<String, Tuple> groupedByPid) {
        groupedByPid.forEach((pid, tuple) -> {
            LogEntry startEntry = tuple.start;
            LogEntry endEntry = tuple.end;

            // Handle missing entries
            if (startEntry != null && endEntry != null) {
                // Compute the duration if both START and END entries are present
                long durationInSeconds = Duration.between(startEntry.time(), endEntry.time()).getSeconds();

                if (durationInSeconds > ERROR_LOG_THRESHOLD) {
                    logger.error("Job '{}' with PID {} took longer than 10 minutes.",
                            startEntry.description(), pid);
                } else if (durationInSeconds > WARNING_LOG_THRESHOLD) {
                    logger.warn("Job '{}' with PID {} took longer than 5 minutes.",
                            startEntry.description(), pid);
                }
            }
        });
    }


    static class Tuple {
        LogEntry start;
        LogEntry end;

        public Tuple(LogEntry start, LogEntry end) {
            this.start = start;
            this.end = end;
        }

        // Put the entry in the correct slot (START or END)
        public static Tuple create(LogEntry logEntry) {
            return logEntry.status() == Status.START
                    ? new Tuple(logEntry, null)
                    : new Tuple(null, logEntry);
        }

        // Merges two Tuples by placing entries in the correct slot
        public static Tuple merge(Tuple existing, Tuple incoming) {
            if (incoming.start != null) existing.start = incoming.start; // Merge START
            if (incoming.end != null) existing.end = incoming.end;       // Merge END
            return existing;
        }

    }
}
