package com.example.labcontrolapp;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class DeviceManager {
    private ArrayList<Device> devicesList = new ArrayList<>();
    private final MainActivity mainActivity;

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
    }


    public void connectDevices(DeviceAdapter adapter, OnDevicesConnectedCallback onFinishedCallback) {
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

}
