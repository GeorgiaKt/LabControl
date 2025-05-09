package com.example.labcontrolapp;

public class Device {
    private String name;
    private String ipAddress;
    private String os;
    private String status;
    private String macAddress;

    public Device(String name, String ip, String mac) {
        this.name = name;
        this.ipAddress = ip;
        this.os = "Unknown";
        this.status = "Unknown";
        this.macAddress = mac;
    }

    public Device(String name, String ip, String os, String status, String mac) {
        this.name = name;
        this.ipAddress = ip;
        this.os = os;
        this.status = status;
        this.macAddress = mac;
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
}
