package com.example.labcontrolapp;

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

}
