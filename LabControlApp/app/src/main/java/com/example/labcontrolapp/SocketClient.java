package com.example.labcontrolapp;

import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;

public class SocketClient {
    private String serverIP;
    private Socket comSocket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;


    public boolean connect(String ip) {
        serverIP = ip;
        try {
            comSocket = new Socket(); //create an empty socket for the communication between server - client
            SocketAddress serverAddress = new InetSocketAddress(serverIP, Constants.SERVER_PORT);
            comSocket.connect(serverAddress, Constants.CONNECT_TIMEOUT); // connect to server with a 5 seconds timeout

            comSocket.setSoTimeout(Constants.CONNECT_TIMEOUT); // set a timeout in case the object stream blocks the app

            outputStream = new ObjectOutputStream(comSocket.getOutputStream());
            inputStream = new ObjectInputStream(comSocket.getInputStream());

            Log.d("SocketClient", "Connected to /" + serverIP + ":" + Constants.SERVER_PORT);
            return true;

        } catch (IOException e) {
            Log.e("SocketClient", "Failed to connect to /" + serverIP + ":" + Constants.SERVER_PORT);

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

        } catch (IOException e) {
            Log.e("SocketClient", "Failed to disconnect from /" + serverIP + ":" + Constants.SERVER_PORT);
        } finally {
            // removing references
            outputStream = null;
            inputStream = null;
            comSocket = null;
        }
    }

    public void sendMessage(String message) {
        try {
            outputStream.writeObject(message);
            outputStream.flush();
        } catch (IOException e) {
            Log.e("SocketClient", "Failed to send message");
        }
    }

    public String receiveMessage() {
        String response = "";
        try {
            response = (String) inputStream.readObject();
        } catch (ClassNotFoundException | IOException e) {
            Log.e("SocketClient", "Failed to receive message");
        }
        return response;
    }

    public void setReadTimeout(int timeout) { // use this method to change read timeout whenever restore option is selected
        try {
            if (comSocket != null && !comSocket.isClosed())
                comSocket.setSoTimeout(timeout);
        } catch (SocketException e) {
            Log.e("SocketClient", "Failed to set a new timeout");
        }
    }
}
