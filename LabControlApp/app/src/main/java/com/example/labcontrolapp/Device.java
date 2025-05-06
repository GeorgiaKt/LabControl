package com.example.labcontrolapp;

public class Device {
    private String name;
    private String os;
    private boolean status;
    private String macAddress;

    public Device(){
        name = "Unknown";
        os = "Unknown";
        macAddress = "Unknown";
    }

    public Device(String name, String os, boolean status, String macAddress){
        this.name = name;
        this.os = os;
        this.status = status;
        this.macAddress = macAddress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }
}
