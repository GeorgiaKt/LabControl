package com.example.labcontrolapp;

import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class SocketClient {
    private final MainActivity mainActivity;
    private final String serverIP = "10.0.2.2"; // 10.0.2.2 emulator's ip
    private final int serverPort = 41007;
    private Socket comSocket;
    SocketAddress serverAddress;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;

    public SocketClient(MainActivity mainActivity) {
        // keep reference to the main activity
        this.mainActivity = mainActivity;
    }


    public boolean connect() {
        try {
            comSocket = new Socket(); //create an empty socket for the communication between server - client
            serverAddress = new InetSocketAddress(serverIP, serverPort);
            comSocket.connect(serverAddress, 5000); // connect to server with a 5 seconds timeout

            outputStream = new ObjectOutputStream(comSocket.getOutputStream());
            inputStream = new ObjectInputStream(comSocket.getInputStream());

            Log.d("SocketClient","Connected to /" + serverIP + ":" + serverPort);
            mainActivity.displayToast("Connected to Server");
            return true;

        } catch (IOException e) {
            Log.e("SocketClient", "Failed to connect at port: " + serverPort, e);
            mainActivity.displayToast("Failed to connect Server");
            // turn off switch in case of a failed connection
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mainActivity.switchConnection.setChecked(false);
                }
            });

            return false;
        }
    }

    public void disconnect(){
        try {
            if (comSocket != null && !comSocket.isClosed()) {
                comSocket.close();
                Log.d("SocketClient", "Disconnected from /" + serverIP + ":" + serverPort);
                mainActivity.displayToast("Disconnected from Server");
            }
            if (outputStream != null)
                outputStream.close();
            if (inputStream != null)
                inputStream.close();

        } catch (IOException e) {
            Log.e("SocketClient", "Failed to disconnect from /" + serverIP + ":" + serverPort, e);
            mainActivity.displayToast("Failed to disconnect from Server");
        } finally {
            // removing references
            outputStream = null;
            inputStream = null;
            comSocket = null;
        }
    }

}
