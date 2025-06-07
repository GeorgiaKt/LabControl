package com.example.labcontrolapp;

import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DeviceManager {
    private ArrayList<Device> devicesList = new ArrayList<>();
    private final MainActivity mainActivity;
    private DeviceAdapter adapter;
    private final ExecutorService messageExecutor = Executors.newFixedThreadPool(27); // use 27 threads
    private final ExecutorService echoExecutor = Executors.newFixedThreadPool(27);
    private final ArrayList<String> responsesList = new ArrayList<>();

    public DeviceManager(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public ArrayList<String> getResponsesList() {
        return responsesList;
    }

    public ArrayList<Device> getDevicesList() {
        return devicesList;
    }

    public void setDevicesList(ArrayList<Device> devicesList) {
        this.devicesList = devicesList;
    }

    public void attachDeviceAdapter(DeviceAdapter adapter) {
        this.adapter = adapter;
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
    }

    public ArrayList<Device> getSelectedDevices() {
        ArrayList<Device> selectedDev = new ArrayList<>();
        for (Device dev : devicesList)
            if (dev.isSelected())
                selectedDev.add(dev);
        return selectedDev;
    }

    public ArrayList<Integer> getSelectedDevicesPositions() {
        ArrayList<Integer> selectedDevicesPositions = new ArrayList<>();
        for (int i = 0; i < devicesList.size(); i++)
            if (devicesList.get(i).isSelected())
                selectedDevicesPositions.add(i);
        return selectedDevicesPositions;
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

    public void selectAll() {
        for (int i = 0; i < devicesList.size(); i++) {
            devicesList.get(i).setSelected(true);
        }
    }

    public boolean areAllSelected() {
        for (Device device : devicesList) {
            if (!device.isSelected()) {
                return false;
            }
        }
        return true;
    }

    public void handleMessageExchange(String message) { // exchange messages with selected devices
        ArrayList<Device> selectedDevices = getSelectedDevices();
        ArrayList<Integer> selectedDevPositions = getSelectedDevicesPositions();
        for (int i = 0; i < selectedDevices.size(); i++) {
            Device dev = selectedDevices.get(i); // selected device
            int pos = selectedDevPositions.get(i); // position of the selected device

            messageExecutor.submit(() -> {
                synchronized (dev.getClient()) { // synchronize per device's socket, one thread at a time can access the device's socket
                    // connect to device
                    connectDevice(dev);
                    // send message if online
                    if (dev.getStatus().equals(Constants.STATUS_ONLINE)) { // send the command only if the selected device is online
                        communicateWithDevice(message, dev);
                    }
                    // disconnect from device
                    disconnectDevice(dev);
                    updateDeviceUI(pos);
                }
            });
        }
    }

    private void updateDeviceUI(int pos) {
        // update ui of the selected device
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyItemChanged(pos);
            }
        });
    }

    public void echoAllDevices() {
        for (int i = 0; i < devicesList.size(); i++) {
            Device dev = devicesList.get(i);
            int pos = i;

            echoExecutor.submit(() -> {
                synchronized (dev.getClient()) {
                    connectDevice(dev);
                    if (dev.getStatus().equals(Constants.STATUS_ONLINE)) { // send the command only if the selected device is online
                        communicateWithDevice(Constants.COMMAND_ECHO, dev);
                    }
                    disconnectDevice(dev);
                    updateDeviceUI(pos);
                }
            });
        }
    }

    private void communicateWithDevice(String message, Device dev) {
        dev.getClient().sendMessage(message);
        String response = dev.getClient().receiveMessage();
        handleResponse(dev, message, response);
        if (message.equals(Constants.COMMAND_RESTORE)) { // when restore command is sent, app receives 2 responses
            try { // in case failure occurs, reset timeout
                dev.getClient().setReadTimeout(Constants.READ_TIMEOUT); // increase to read timeout in order to receive 2nd response for restore
                response = dev.getClient().receiveMessage();
                handleResponse(dev, message, response);
            } catch (Exception e) {
                Log.e("DeviceManager - RESTORE ERROR", "Failed to receive \"Restored\".");
            } finally {
                dev.getClient().setReadTimeout(Constants.CONNECT_TIMEOUT); // reset timeout after receiving 2nd response
            }
        }
    }

    private static void connectDevice(Device dev) {
        boolean connectionResult = dev.getClient().connect(Constants.DEFAULT_SERVER_IP); // connect( dev.getIpAddress() )
        if (connectionResult)
            dev.setStatus(Constants.STATUS_ONLINE);
        else
            dev.setStatus(Constants.STATUS_OFFLINE);
    }

    private static void disconnectDevice(Device dev) {
        if (dev.getClient() != null)
            dev.getClient().disconnect();
    }

    public void handleResponse(Device dev, String message, String response) {
        // based on the command executed do the corresponding actions in app
        if (response.contains(" - ")) {
            String[] parts = response.split(" - ");
            dev.setName(parts[0]); // update pc name
            switch (message) {
                case Constants.COMMAND_ECHO:
                    // update os
                    if (parts[1].contains("Windows")) {
                        dev.setOs("Windows");
                    } else
                        dev.setOs("Linux");
                    Log.d("DeviceManager - ECHO", response);
                    break;
                case Constants.COMMAND_RESTART:
                    Log.d("DeviceManager - RESTART", response);
                    break;
                case Constants.COMMAND_SHUTDOWN:
                    dev.setStatus(Constants.STATUS_OFFLINE); // update status on screen
                    Log.d("DeviceManager - SHUTDOWN", response);
                    break;
                case Constants.COMMAND_RESTORE:
                    Log.d("DeviceManager - RESTORE", response);
                    break;
            }
            responsesList.add(response);
        }
    }

    public void shutdownExecutors() {
        shutdownExecutor(messageExecutor);
        shutdownExecutor(echoExecutor);
    }

    private void shutdownExecutor(ExecutorService executor) {
        executor.shutdown(); // stop the executor from accepting new tasks
        try {
            // wait up to 6 seconds for existing tasks to finish
            if (!executor.awaitTermination(6, java.util.concurrent.TimeUnit.SECONDS)) {
                executor.shutdownNow(); // if they didn't finish within the timeout, shut down executor by interrupting them
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt(); // restore interrupted status
        }
    }

}
