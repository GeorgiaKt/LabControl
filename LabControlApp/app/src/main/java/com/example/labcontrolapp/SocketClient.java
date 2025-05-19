package com.example.labcontrolapp;

import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class SocketClient {
    private String serverIP;
    private Socket comSocket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private boolean isConnected = false;

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public boolean connect(String ip) {
        serverIP = ip;
        try {
            comSocket = new Socket(); //create an empty socket for the communication between server - client
            SocketAddress serverAddress = new InetSocketAddress(serverIP, Constants.SERVER_PORT);
            comSocket.connect(serverAddress, Constants.CONNECT_TIMEOUT); // connect to server with a 5 seconds timeout

            comSocket.setSoTimeout(Constants.CONNECT_TIMEOUT); // set a timeout in case the object stream blocks the app

            outputStream = new ObjectOutputStream(comSocket.getOutputStream());
            inputStream = new ObjectInputStream(comSocket.getInputStream());

            setConnected(true);

            Log.d("SocketClient", "Connected to /" + serverIP + ":" + Constants.SERVER_PORT);
            return true;

        } catch (IOException e) {
            setConnected(false);
            Log.e("SocketClient", "Failed to connect at port: " + Constants.SERVER_PORT, e);

            return false;
        }
    }

    public void disconnect() {
        try {
            if (comSocket != null && !comSocket.isClosed()) {
                comSocket.close();
                Log.d("SocketClient", "Disconnected from /" + serverIP + ":" + Constants.SERVER_PORT);
            }
            if (outputStream != null)
                outputStream.close();
            if (inputStream != null)
                inputStream.close();

            setConnected(false);

        } catch (IOException e) {
            Log.e("SocketClient", "Failed to disconnect from /" + serverIP + ":" + Constants.SERVER_PORT, e);
        } finally {
            // removing references
            outputStream = null;
            inputStream = null;
            comSocket = null;
        }
    }

    public synchronized void sendMessage(String message) { // synchronized since being accessed via multiple threads
        try {
            outputStream.writeObject(message);
            outputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized String receiveMessage() {
        String response;
        try {
            response = (String) inputStream.readObject();
        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException(e);
        }
        return response;
    }

}
