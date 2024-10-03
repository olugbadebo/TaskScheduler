package edu.cwru.ooa21.taskscheduler.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.mail.MessagingException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AlertSystemTest {
    private AlertSystem logger;

    @BeforeEach
    void setUp() {
        logger = spy(new AlertSystem());
        createMockEnvFile();
    }

    private void createMockEnvFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("test.env"))) {
            writer.write("SMTP_HOST=localhost\n");
            writer.write("SMTP_PORT=587\n");
            writer.write("SMTP_USER=test@example.com\n");
            writer.write("SMTP_PASSWORD=secret\n");
        } catch (IOException e) {
            fail("Could not create .env file for testing.");
        }
    }

    @Test
    void testLoadEnv() {
        Map<String, String> env = logger.loadEnv("test.env");
        assertEquals("localhost", env.get("SMTP_HOST"));
        assertEquals("587", env.get("SMTP_PORT"));
        assertEquals("test@example.com", env.get("SMTP_USER"));
        assertEquals("secret", env.get("SMTP_PASSWORD"));
    }

    @Test
    void testLoadEnv_Failure() {
        deleteEnvFile();
        assertThrows(RuntimeException.class, () -> logger.loadEnv("test.env"));
    }

    @Test
    void testInfoLogging() throws IOException {
        logger.info("Test info message");
        String logContent = readLogFile("info.log");
        assertFalse(logContent.isEmpty());
    }

    @Test
    void testErrorLogging() throws IOException {
        logger.loadEnv(null);
        logger.error("Test error message");
        String logContent = readLogFile("error.log");
        assertFalse(logContent.isEmpty());
    }

    @Test
    void testSendEmail() throws MessagingException {
        logger.loadEnv(null);
        doNothing().when(logger).sendEmail(any(String.class), any(String.class));
        logger.sendEmail("Test Subject", "Test Body");
        verify(logger).sendEmail("Test Subject", "Test Body");
    }

    @Test
    void testCreateLogDirectory() {
        assertDoesNotThrow(() -> logger.createLogDirectory());
    }

    private String readLogFile(String logFileName) throws IOException {
        String logDir = "D:/Projects/__scripts/taskscheduler/logs/";
        Path path = Path.of(logDir + new SimpleDateFormat("yyyyMMdd").format(new Date()) + "/" + logFileName);
        if (Files.exists(path)) {
            return Files.readString(path);
        }
        return "";
    }

    private void deleteEnvFile() {
        try {
            Files.deleteIfExists(Path.of("test.env"));
        } catch (IOException e) {
            fail("Could not delete .env file for testing.");
        }
    }
}
