package edu.cwru.ooa21.taskscheduler.models;

import edu.cwru.ooa21.taskscheduler.enums.TaskPriority;
import edu.cwru.ooa21.taskscheduler.interfaces.impl.DependentTask;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.BindException;
import java.net.Socket;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RemoteServerTest {
    private RemoteServer remoteServer1;
    private DependentTask dependentTask1;
    int remoteServerPort1 = 3000;

    @BeforeEach
    void setUp() throws IOException {
        remoteServer1 = new RemoteServer("remoteServer1", remoteServerPort1, 3);
        remoteServer1.startServer();
        dependentTask1 = new DependentTask("dTask1", Duration.ofMillis(1000), TaskPriority.HIGH, Duration.ofMillis(100000), Set.of());
    }

    @AfterEach
    void tearDown() throws IOException {
        if (remoteServer1 != null)
            remoteServer1.stopServer();
    }

    @Test
    void testAddTaskViaSocket() throws IOException, ClassNotFoundException {
        try (Socket clientSocket = new Socket("localhost", remoteServerPort1);
             ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
             ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream())) {

            outputStream.writeObject("ADD_TASK");
            String response1 = (String) inputStream.readObject();
            assertEquals("Send Task", response1);

            outputStream.writeObject(dependentTask1);
            String response2 = (String) inputStream.readObject();
            assertEquals("Task added successfully.", response2);

            assertEquals(remoteServer1.getTaskCount(), 1);
        }
    }

    @Test
    void testAddTaskViaSocketSendingInvalidTask() throws IOException, ClassNotFoundException {
        try (Socket clientSocket = new Socket("localhost", remoteServerPort1);
             ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
             ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream())) {

            outputStream.writeObject("ADD_TASK");
            String response1 = (String) inputStream.readObject();
            assertEquals("Send Task", response1);

            outputStream.writeObject(null);

            String response = (String) inputStream.readObject();
            assertEquals("Task failed to add. Invalid task", response);
            assertEquals(remoteServer1.getTaskCount(), 0);
        }
    }

    @Test
    void testReceiveMessageOnInvalidCommand() throws IOException, ClassNotFoundException {
        try (Socket clientSocket = new Socket("localhost", remoteServerPort1);
             ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
             ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream())) {

            outputStream.writeObject("INVALID_COMMAND");

            String response = (String) inputStream.readObject();
            assertEquals("Invalid command", response);
            assertEquals(remoteServer1.getTaskCount(), 0);
        }
    }

    @Test
    void testServerSocketIsRunning() {
        assertTrue(remoteServer1.isRunning());
    }

    @Test
    void testServerStartedOnOccupiedPort() throws IOException {
        assertThrows(BindException.class, () -> {
            RemoteServer remoteServer2 = new RemoteServer("remoteServer3", remoteServerPort1, 3);
        });
    }

}
