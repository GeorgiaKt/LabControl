package com.example.labcontrolapp;

import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class DeviceManager {
    private ArrayList<Device> devicesList = new ArrayList<>();
    private final MainActivity mainActivity;
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    public DeviceManager(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public ArrayList<Device> getDevicesList() {
        return devicesList;
    }

    public void setDevicesList(ArrayList<Device> devicesList) {
        this.devicesList = devicesList;
    }

    public void initializeDevices() {
        devicesList.clear();
        String name;
        String ip;
        String mac;
        for (int i = 1; i < 28; i++) {
            if (i < 10) {
                name = "PRPC0" + i;
                ip = "192.168.88.0" + i;
                mac = "245:34:1C:4T:" + i;
            } else {
                name = "PRPC" + i;
                ip = "192.168.88." + i;
                mac = "245:34:1C:4T:" + i;
            }
            Device dev = new Device(name, ip, mac);
            dev.attachSocketClient(new SocketClient()); // attach socket client to each device
            devicesList.add(dev);
        }

//        Device device = new Device("Desktop", "192.168.1.1", "idkmac");
//        device.attachSocketClient(new SocketClient());
//        devicesList.add(device);
    }


    public void connectDevices(DeviceAdapter adapter, OnDevicesCallback onFinishedCallback) {
        AtomicInteger completed = new AtomicInteger(0); // thread-safe counter for the number of threads that completed the connection
        int total = devicesList.size();

        for (int i = 0; i < devicesList.size(); i++) {
            final int index = i;
            Device dev = devicesList.get(i);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean connectionResult = dev.getClient().connect(Constants.DEFAULT_SERVER_IP); // connect( dev.getIpAddress() )
                    if (connectionResult)
                        dev.setStatus("Online");
                    else
                        dev.setStatus("Offline");

                    mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyItemChanged(index); // update ui
                            if (completed.incrementAndGet() == total) {
                                onFinishedCallback.onAllDevicesConnected(); // inform when all finish
                            }
                        }
                    });
                }
            }).start();

        }

    }

    public void disconnectDevices() {
        // close socket connection if still connected
        for (Device dev : devicesList) {
            if (dev.getClient() != null) {
                dev.getClient().disconnect();
                dev.setClient(null);
            }
        }
    }

    public ArrayList<Device> getSelectedDevices() {
        ArrayList<Device> selectedDev = new ArrayList<>();
        for (Device dev : devicesList)
            if (dev.isSelected())
                selectedDev.add(dev);
        return selectedDev;
    }

    public void clearSelection() {
        for (int i = 0; i < devicesList.size(); i++) {
            devicesList.get(i).setSelected(false);
        }
    }

    public void toggleSelection(int position) {
        boolean sel = devicesList.get(position).isSelected();
        devicesList.get(position).setSelected(!sel); // toggle selection of the device
    }

    public void handleMessageExchange(String message) { // exchange messages with selected devices
        ArrayList<Device> selectedDevices = getSelectedDevices();
        for (Device dev : selectedDevices) {
            if (dev.getStatus().equalsIgnoreCase("online")) { // send the command only if the selected device is online
                executor.submit(() -> {
                    dev.getClient().sendMessage(message);
                    String response = dev.getClient().receiveMessage();
//                    if (message.equalsIgnoreCase("restore")) // when restore command is sent, app receives 2 responses
//                        response = dev.getClient().receiveMessage();
                    handleResponse(dev, message, response);
                });
            }
        }


    }

    private void handleResponse(Device dev, String message, String response) {
        switch (message) {
            case "echo":
                updateInfo(dev, response);
        }
    }

    private void updateInfo(Device dev, String response) {
        String[] parts = response.split(" - ");
        if (parts.length == 2) {
            dev.setName(parts[0]);
            dev.setOs(parts[1]);
            Log.d("DeviceManager Echo ", dev.getName() + dev.getOs());
        }
    }


}
