import com.lseg.logmonitor.LogMonitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static com.lseg.logmonitor.LogMonitor.FILE_PATH;
import static org.mockito.Mockito.*;

class LogMonitorTest {
    private LogMonitor logMonitor;

    @Mock
    private Logger mockLogger;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        logMonitor = new LogMonitor();
        LogMonitor.logger = mockLogger;
    }

    @Test
    void testGenerateOutputWithValidData() {
        Stream<String> logLines = Stream.of(
                "11:35:23,scheduled task 051,START,39547",
                "11:46:00,scheduled task 051,END,39547",  // 10-minute and 37 seconds duration (exceeds 10 minutes - ERROR)
                "12:00:00,scheduled task 052,START,47981",
                "12:06:00,scheduled task 052,END,47981",  // 6-minute duration (exceeds 5 minutes - WARN)
                "13:00:00,scheduled task 053,START,58123",
                "13:03:00,scheduled task 053,END,58123"   // 3-minute duration (no warning or error)
        );

        try (var mockedLines = mockStatic(Files.class)) {
            mockedLines.when(() -> Files.lines(Path.of(FILE_PATH)))
                    .thenReturn(logLines);

            logMonitor.generateOutput();

            verify(mockLogger, times(1)).error(
                    "Job '{}' with PID {} took longer than 10 minutes.",
                    "scheduled task 051", "39547"
            );
            verify(mockLogger, times(1)).warn(
                    "Job '{}' with PID {} took longer than 5 minutes.",
                    "scheduled task 052", "47981"
            );
            verify(mockLogger, never()).warn(anyString(), eq("scheduled task 053"), eq("58123"));
            verify(mockLogger, never()).error(anyString(), eq("scheduled task 053"), eq("58123"));
        }
    }

    @Test
    void testGenerateOutputWithIncompleteEntries() {
        Stream<String> logLines = Stream.of(
                "11:35:23,scheduled task 032,START,37980"
        );

        try (var mockedLines = mockStatic(Files.class)) {
            mockedLines.when(() -> Files.lines(Path.of(FILE_PATH)))
                    .thenReturn(logLines);

            logMonitor.generateOutput();

            verify(mockLogger, never()).warn(anyString(), any(), any());
            verify(mockLogger, never()).error(anyString(), any(), any());
        }
    }

    @Test
    void testGenerateOutputWithNoData() {
        Stream<String> emptyStream = Stream.empty();

        try (var mockedLines = mockStatic(Files.class)) {
            mockedLines.when(() -> Files.lines(Path.of(FILE_PATH)))
                    .thenReturn(emptyStream);

            logMonitor.generateOutput();

            verify(mockLogger, never()).warn(anyString(), any(), any());
            verify(mockLogger, never()).error(anyString(), any(), any());
        }
    }

    @Test
    void testGenerateOutputWithException() {
        try (var mockedLines = mockStatic(Files.class)) {
            mockedLines.when(() -> Files.lines(Path.of(FILE_PATH)))
                    .thenThrow(new RuntimeException("File read error"));

            logMonitor.generateOutput();

            verify(mockLogger, times(1)).error(
                    eq("Error reading the log file. File path: {}. Error: {}"),
                    eq("src/main/resources/logs.log"),
                    eq("File read error"),
                    any(Throwable.class)
            );
        }
    }
}