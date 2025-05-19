package com.example.labcontrolapp;

import android.util.Log;

import androidx.annotation.NonNull;

public class Device {
    private String name;
    private String ipAddress;
    private String os;
    private String status;
    private String macAddress;
    private SocketClient client;
    private boolean isSelected;

    public Device(String name, String ip, String mac) {
        this.name = name;
        this.ipAddress = ip;
        this.os = "N/A OS";
        this.status = "N/A Status";
        this.macAddress = mac;
    }

    public Device(String name, String ip, String os, String status, String mac) {
        this.name = name;
        this.ipAddress = ip;
        this.os = os;
        this.status = status;
        this.macAddress = mac;
    }

    public void attachSocketClient(SocketClient client) {
        this.client = client;
    }

    public boolean compareDevices(Device dev2) { // compare if two devices are the same
        return this.name.equalsIgnoreCase(dev2.name) &&
                this.ipAddress.equalsIgnoreCase(dev2.ipAddress) &&
                this.os.equalsIgnoreCase(dev2.os) &&
                this.status.equalsIgnoreCase(dev2.status) &&
                this.macAddress.equalsIgnoreCase(dev2.macAddress);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public SocketClient getClient() {
        return client;
    }

    public void setClient(SocketClient client) {
        this.client = client;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    @NonNull
    @Override
    public String toString() {
        return getName() + " " +
                getIpAddress() + " " +
                getOs() + " " +
                getStatus() + " " +
                getMacAddress() + " ";
    }


    private void handleResponse(String response) {
        if (response.contains(" - ")) {
            // split to parts to determine the command executed
            String[] parts = response.split(" - ");
            String message = "";
            if (parts.length == 2) {
                switch (parts[1]) {
                    case "Rebooting...":
                        message = Constants.COMMAND_RESTART;
                        break;
                    case "Shutting down...":
                        message = Constants.COMMAND_SHUTDOWN;
                        break;
                    case "Restoring...":
                        message = Constants.COMMAND_RESTORE;
                        break;
                    case "Restored":
                        message = Constants.COMMAND_RESTORE;
                        break;
                    default:
                        message = Constants.COMMAND_ECHO;
                        break;

                }

                // based on the command executed do the corresponding actions in app
                switch (message) {
                    case Constants.COMMAND_ECHO:
                        setName(parts[0]);
                        setOs(parts[1]);
                        break;
                    case Constants.COMMAND_RESTART:
                        Log.d("DeviceManager RESTART RESPONSE", response);
                        break;
                    case Constants.COMMAND_SHUTDOWN:
                        Log.d("DeviceManager SHUTDOWN RESPONSE", response);
                        break;
                    case Constants.COMMAND_RESTORE:
                        Log.d("DeviceManager RESTORE RESPONSE", response);
                        break;
                }
            }
        }
    }

}
