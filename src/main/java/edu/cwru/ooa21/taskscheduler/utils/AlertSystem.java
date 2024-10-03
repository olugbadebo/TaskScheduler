package edu.cwru.ooa21.taskscheduler.utils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class AlertSystem {
    private String logDirectory = "D:/Projects/__scripts/taskscheduler/";

    private final String host;
    private final String port;
    private final String user;
    private final String password;

    public AlertSystem() {
        String today = new SimpleDateFormat("yyyyMMdd").format(new Date());
        logDirectory += "logs/" + today;
        createLogDirectory();

        Map<String, String> env = loadEnv(null);
        this.host = env.get("SMTP_HOST");
        this.port = env.get("SMTP_PORT");
        this.user = env.get("SMTP_USERNAME");
        this.password = env.get("SMTP_PASSWORD");
    }

    public Map<String, String> loadEnv(String filename) {
        Map<String, String> env = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename != null ? filename : ".env"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("=");
                if (parts.length == 2) {
                    env.put(parts[0].trim(), parts[1].trim());
                }
            }
            info("Environment variables loaded successfully.");
        } catch (IOException e) {
            errorNoEmail("Failed to load environment variables: " + e.getMessage());
            throw new RuntimeException("Failed to load environment variables from .env file.");
        }
        return env;
    }

    public void sendEmail(String subject, String body) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);

        Session session = Session.getInstance(props,
            new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(user, password);
                }
            });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(user));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(user));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
            info("Email sent successfully to " + user + "!");

        } catch (MessagingException e) {
            handleEmailError(e);
        }
    }

    private void handleEmailError(MessagingException e) {
        String errorMessage = String.format(
            "____________________________________________________________________________________________________________________\n" +
                "                                                                                                                 \n" +
                "                                              ERROR: EMAIL FAILED TO SEND!                                         \n" +
                "                                                                                                                 \n" +
                "____________________________________________________________________________________________________________________\n" +
                "ERRORERRORERRORERRORERRORERRORERRORERRORERRORERRORERRORERRORERRORERRORERRORERRORERRORERRORERRORERRORERRORERRORERRORERROR\n" +
                "                                                                                                                 \n" +
                "                   An error occurred while attempting to send the email: \"Failed to send email: %s\"               \n" +
                "                                                                                                                 \n" +
                "ERRORERRORERRORERRORERRORERRORERRORERRORERRORERRORERRORERRORERRORERRORERRORERRORERRORERRORERRORERRORERRORERRORERRORERROR\n" +
                "____________________________________________________________________________________________________________________",
            e.getMessage()
        );

        System.err.println(errorMessage);
        errorNoEmail("Failed to send email: " + e.getMessage());
    }

    public void createLogDirectory() {
        File directory = new File(logDirectory);
        if (!directory.exists()) {
            if (directory.mkdirs()) {
                System.err.println("Failed to create logs directory");
            }
        }
    }

    private void writeLog(String level, String message) {
        String logFileName = logDirectory + "/" + level + ".log";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFileName, true))) {
            writer.write(getCurrentTimeStamp() + " - " + level.toUpperCase() + ": " + message);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Failed to write to log file: " + e.getMessage());
        }
    }

    public void debug(String message) {
        writeLog("debug", message);
    }

    public void info(String message) {
        writeLog("info", message);
    }

    public void warn(String message) {
        writeLog("warn", message);
    }

    public void errorNoEmail(String message) {
        writeLog("error", message);
    }

    public void error(String message) {
        String currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        writeLog("error", message);
        sendEmail(
            "ALERT! ERROR TRIGGERED",
            "The following error was thrown at %s:\n%s".formatted(currentDateTime, message)
        );
    }

    public String getCurrentTimeStamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }
}
