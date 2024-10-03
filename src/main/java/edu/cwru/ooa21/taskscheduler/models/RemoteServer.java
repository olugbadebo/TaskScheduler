package edu.cwru.ooa21.taskscheduler.models;

import edu.cwru.ooa21.taskscheduler.interfaces.impl.DependentTask;
import edu.cwru.ooa21.taskscheduler.utils.AlertSystem;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class RemoteServer extends Server implements Runnable {
    private ServerSocket socket;
    private boolean running = false;
    private Thread serverThread;
    private int port;
    private final AlertSystem logger = new AlertSystem();
    public RemoteServer(String serverId, int port, int failureThreshold) throws IOException {
        super(serverId, failureThreshold);
        this.port = port;
        this.socket = new ServerSocket(port);
    }

    public int getPort() {
        return this.port;
    }

    public boolean isRunning() {
        return this.running;
    }

    @Override
    public void run() {
        this.running = true;

        while (running) {
            try (Socket clientSocket = socket.accept();
                 ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream());
                 ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream())) {

                if (inputStream.readObject() instanceof String command) {
                    switch (command) {
                        case "ADD_TASK":
                            outputStream.writeObject("Send Task");
                            if (inputStream.readObject() instanceof DependentTask dependentTask) {
                                addTask(dependentTask);
                                outputStream.writeObject("Task added successfully.");
                            } else {
                                outputStream.writeObject("Task failed to add. Invalid task");
                            }
                            break;

                        default:
                            outputStream.writeObject("Invalid command");
                            break;
                    }
                } else {
                    outputStream.writeObject("Invalid command");
                }

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                break;
            } catch (IOException e) {
                logger.error("Error processing client request: " + e.getMessage());
                break;
            }
        }

        this.running = false;
    }

    public void startServer() {
        if (serverThread == null || !serverThread.isAlive())
            serverThread = new Thread(this);
        serverThread.start();
        logger.info("Server %s started, listening to port %d".formatted(this.getServerId(), this.getPort()));
    }

    public void stopServer() throws IOException {
        this.running = false;
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        if (serverThread != null && serverThread.isAlive()) {
            serverThread.interrupt();
        }
        logger.info("Server %s stopped, freeing port %d".formatted(this.getServerId(), this.getPort()));
    }

    public Thread getServerThread() {
        return serverThread;
    }
}
