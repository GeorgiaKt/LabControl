import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class LabControlServer {
    private final int port;
    private ServerSocket serverSocket;
    private Socket comSocket;
    private String clientIP;
    private int clientPort;

    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;

    private boolean serverRunning = false;

    public LabControlServer() {
        this.port = 41007; // server's port
        try {
            this.serverSocket = new ServerSocket(this.port); //create socket for the communication
        } catch (IOException e) {
            System.out.println("Could not listen on port: " + this.port);
        }
        serverRunning = true;
    }


    public static void main(String[] args) {
        LabControlServer labControlServer = new LabControlServer();
        if (labControlServer.serverRunning) {
            System.out.println("Server is running..");
            while (true) {
                labControlServer.establishSocketConnection();
                while (true) {
                    String command = null;
                    try {
                        command = labControlServer.receiveMessage();
                    } catch (RuntimeException e) {
                        System.out.println("Client disconnected or error: " + e.getMessage());
                        break; // break inner loop
                    }

                    if (command == null) {
                        System.out.println("Client disconnected.");
                        break; // null means client disconnected
                    }
                    switch (command) {
                        case "echo":
                            System.out.println("Echo from: " + labControlServer.clientIP + ":" + labControlServer.clientPort);
                            labControlServer.sendMessage(labControlServer.getNetworkName() + " - " + labControlServer.getOperatingSystem());
                            System.out.println("Sent echo");
                            break;
                        case "restart":
                            System.out.println("Restart from: " + labControlServer.clientIP + ":" + labControlServer.clientPort);
                            labControlServer.sendMessage(labControlServer.getSystemName() + " - Rebooting...");
                            System.out.println("Sent restart");
                            break;
                        case "shutdown":
                            System.out.println("Shut down from: " + labControlServer.clientIP + ":" + labControlServer.clientPort);
                            labControlServer.sendMessage(labControlServer.getSystemName() + " - Shutting down...");
                            System.out.println("Sent shutdown");
                            break;
                        case "restore":
                            System.out.println("Restore from: " + labControlServer.clientIP + ":" + labControlServer.clientPort);
                            labControlServer.sendMessage(labControlServer.getNetworkName() + " - Restoring...");
                            System.out.println("Sent restoring");
                            try {
                                Thread.sleep(120000); // sleep for 120 seconds
                                labControlServer.sendMessage(labControlServer.getNetworkName() + " - Restored");
                                System.out.println("Sent restored");
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }

                            break;
                    }
                }
            }
        }
    }

    private void establishSocketConnection() {
        try {
            comSocket = serverSocket.accept(); //accept client
            // get connected client's info
            clientIP = String.valueOf(comSocket.getInetAddress());
            clientPort = comSocket.getPort();
            System.out.println("Connected to " + clientIP + ":" + clientPort);

            outputStream = new ObjectOutputStream(comSocket.getOutputStream()); //object stream for sending to client
            inputStream = new ObjectInputStream(comSocket.getInputStream()); //object stream for receiving from client

        } catch (IOException e) {
            System.out.println("Failed to connect to " + clientIP + ":" + clientPort);
            System.out.println(e.getMessage());
        }
    }

    private void closeSocketConnection() {
        try {
            if (comSocket != null && !comSocket.isClosed()) {
                comSocket.close();
                System.out.println("Socket connection closed");
            }
            if (outputStream != null)
                outputStream.close();
            if (inputStream != null)
                inputStream.close();

        } catch (IOException e) {
            System.out.println("Failed to close socket connection");
            System.out.println(e.getMessage());
        } finally {
            // removing references
            outputStream = null;
            inputStream = null;
            comSocket = null;
        }
    }

    private String getOperatingSystem() {
        return System.getProperty("os.name");
    }

    private String getNetworkName() {
        String networkName = null;
        try {
            networkName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        return networkName;
    }

    private String getSystemName(){
        String systemName = System.getenv("COMPUTERNAME"); // Windows
        if (systemName == null) {
            systemName = System.getenv("HOSTNAME"); // Unix/Linux/macOS
        }
        return systemName;
    }

    private void sendMessage(String message) {
        try {
            outputStream.writeObject(message);
            outputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String receiveMessage() {
        String response;
        try {
            response = (String) inputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return null;
        }
        return response;
    }
}