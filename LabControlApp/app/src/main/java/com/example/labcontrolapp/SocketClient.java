package com.example.labcontrolapp;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class SocketClient {
    private final String serverIP = "10.0.2.2"; // 10.0.2.2 emulator's ip
    private final int serverPort = 41007;
    private Socket comSocket;
    SocketAddress serverAddress;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;


    public boolean connect() {
        try {
            comSocket = new Socket(); //create an empty socket for the communication between server - client
            serverAddress = new InetSocketAddress(serverIP, serverPort);
            comSocket.connect(serverAddress, 5000); // connect to server with a 5 seconds timeout

            outputStream = new ObjectOutputStream(comSocket.getOutputStream());
            inputStream = new ObjectInputStream(comSocket.getInputStream());

            System.out.println("Connected to /" + serverIP + ":" + serverPort);
            return true;

        } catch (IOException e) {
            System.out.println("Failed to connect at port: " + serverPort);
            System.out.println(e.getMessage());
            return false;
        }
    }

    public void disconnect(){
        try {
            if (comSocket != null && comSocket.isClosed())
                comSocket.close();
            if (outputStream != null)
                outputStream.close();
            if (inputStream != null)
                inputStream.close();
            System.out.println("Disconnected from /" + serverIP + ":" + serverPort);

        } catch (IOException e) {
            System.out.println("Failed to disconnect from /" + serverIP + ":" + serverPort);
        }
    }

}
