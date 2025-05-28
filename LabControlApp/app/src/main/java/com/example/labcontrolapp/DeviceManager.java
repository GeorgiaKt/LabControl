package com.example.labcontrolapp;

import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class DeviceManager {
    private ArrayList<Device> devicesList = new ArrayList<>();
    private final MainActivity mainActivity;
    private DeviceAdapter adapter;
    private final ExecutorService connectionExecutor = Executors.newFixedThreadPool(27); // use 27 threads
    private final ExecutorService messageExecutor = Executors.newFixedThreadPool(27);

    public DeviceManager(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
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

    public void connectDevices(OnDevicesCallback onFinishedCallback) {
        AtomicInteger completed = new AtomicInteger(0); // thread-safe counter for the number of threads that completed the connection
        int total = devicesList.size();

        for (int i = 0; i < devicesList.size(); i++) {
            final int index = i;
            Device dev = devicesList.get(i);

            connectionExecutor.submit(() -> {
                boolean connectionResult = dev.getClient().connect(Constants.DEFAULT_SERVER_IP); // connect( dev.getIpAddress() )
                if (connectionResult)
                    dev.setStatus(Constants.STATUS_ONLINE);
                else
                    dev.setStatus(Constants.STATUS_OFFLINE);

                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyItemChanged(index); // update ui
                        if (completed.incrementAndGet() == total) {
                            onFinishedCallback.onAllDevicesConnected(); // inform when all finish
                        }
                    }
                });
            });
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

    public void handleMessageExchange(String message) { // exchange messages with selected devices
        ArrayList<Device> selectedDevices = getSelectedDevices();
        ArrayList<Integer> selectedDevPositions = getSelectedDevicesPositions();
        for (int i = 0; i < selectedDevices.size(); i++) {
            Device dev = selectedDevices.get(i); // selected device
            int pos = getSelectedDevicesPositions().get(i); // position of the selected device
            if (dev.getStatus().equals(Constants.STATUS_ONLINE)) { // send the command only if the selected device is online
                messageExecutor.submit(() -> {
                    dev.getClient().sendMessage(message);
                    String response = dev.getClient().receiveMessage();
                    handleResponse(dev, message, response);
                    if (message.equals(Constants.COMMAND_RESTORE)) { // when restore command is sent, app receives 2 responses
                        try { // in case failure occurs, reset timeout
                            dev.getClient().setReadTimeout(Constants.READ_TIMEOUT); // increase to read timeout in order to receive 2nd response for restore
                            response = dev.getClient().receiveMessage();
                            handleResponse(dev, message, response);
                        } finally {
                            dev.getClient().setReadTimeout(Constants.CONNECT_TIMEOUT); // reset timeout after receiving 2nd response
                        }
                    }

                    // update ui of the selected device
                    mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyItemChanged(pos);
                        }
                    });
                });
            }
        }

    }

    public void handleResponse(Device dev, String message, String response) {
        // based on the command executed do the corresponding actions in app
        switch (message) {
            case Constants.COMMAND_ECHO:
                if (response.contains(" - ")) {
                    String[] parts = response.split(" - ");
                    if (parts.length == 2) {
                        dev.setName(parts[0]);
                        if (parts[1].contains("Windows")) {
                            dev.setOs("Windows");
                        } else
                            dev.setOs("Linux");
                        Log.d("DeviceManager - ECHO", response);
                    }
                }
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
    }

    public void shutdownExecutors() {
        shutdownExecutor(connectionExecutor);
        shutdownExecutor(messageExecutor);
    }

    private void shutdownExecutor(ExecutorService executor) {
        executor.shutdown(); // stop the executor from accepting new tasks
        try {
            // wait up to 3 seconds for existing tasks to finish
            if (!executor.awaitTermination(3, java.util.concurrent.TimeUnit.SECONDS)) {
                executor.shutdownNow(); // if they didn't finish within the timeout, shut down executor by interrupting them
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt(); // restore interrupted status
        }
    }

}
