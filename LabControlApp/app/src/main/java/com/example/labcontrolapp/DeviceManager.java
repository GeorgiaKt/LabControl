package com.example.labcontrolapp;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
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
        // load and parse devices configuration from JSON file
        try {
            String jsonStr = loadJSON();
            JSONObject jsonObject = new JSONObject(jsonStr);
            JSONArray devicesArray = jsonObject.getJSONArray("devices");

            for (int i = 0; i < devicesArray.length(); i++) {
                JSONObject deviceObj = devicesArray.getJSONObject(i);
                String name = deviceObj.getString("name");
                String networkName = deviceObj.getString("networkName");
                String ip = deviceObj.getString("ip");
                String mac = deviceObj.getString("mac");

                Device dev = new Device(networkName, name, ip, mac);
                dev.attachSocketClient(new SocketClient());
                devicesList.add(dev);
            }

        } catch (JSONException e) {
            Log.e("DeviceManager", "Failed to parse lab_devices_config.json", e);
        }
    }

    private String loadJSON() {
        String json = null; // json will hold all the file contents
        try {
            InputStream input = mainActivity.getAssets().open(Constants.DEVICES_INFO_FILENAME); // open file as a byte stream
            int size = input.available(); // get number of bytes needed
            byte[] buffer = new byte[size];
            int bytesRead = input.read(buffer);
            if (bytesRead != size) {
                Log.e("DeviceManager", "Expected " + size + " bytes, but read " + bytesRead);
            }
            input.close();
            json = new String(buffer, StandardCharsets.UTF_8); // convert from bytes to String
        } catch (IOException e) {
            Log.e("DeviceManager", "Failed to load JSON from assets: " + "lab_devices_config.json");
        }
        return json;
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
            switch (message) {
                case Constants.COMMAND_ECHO:
                    dev.setNetworkName(parts[0]); // update network name of the pc
                    // update os
                    if (parts[1].contains("Windows")) {
                        dev.setOs("Windows");
                    } else
                        dev.setOs("Linux");
                    Log.d("DeviceManager - ECHO", response);
                    break;
                case Constants.COMMAND_RESTART:
                    dev.setName(parts[0]); // update pc name
                    Log.d("DeviceManager - RESTART", response);
                    break;
                case Constants.COMMAND_SHUTDOWN:
                    dev.setName(parts[0]); // update pc name
                    dev.setStatus(Constants.STATUS_OFFLINE); // update status on screen
                    Log.d("DeviceManager - SHUTDOWN", response);
                    break;
                case Constants.COMMAND_RESTORE:
                    dev.setNetworkName(parts[0]); // update pc name
                    Log.d("DeviceManager - RESTORE", response);
                    break;
            }
            responsesList.add(response);

            // append response to bottom sheet
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mainActivity.appendResponseToBottomSheet(response);
                }
            });
        }
    }

    public void handleWakeOnLan() {
        ArrayList<Device> selectedDevices = getSelectedDevices();

        // create multicast lock to allow broadcasting packets
        WifiManager wifiManager = (WifiManager) mainActivity.getSystemService(Context.WIFI_SERVICE);
        WifiManager.MulticastLock lock = wifiManager.createMulticastLock("wol_lock");
        lock.setReferenceCounted(true); // lock gets released only when counter is equal to 0 (acquire increases by 1, release decreases by 1)

        // allow broadcasting & sent magic packet
        lock.acquire();

        CountDownLatch latch = new CountDownLatch(selectedDevices.size());

        for (Device dev : selectedDevices) {
            messageExecutor.submit(() -> {
                if (dev.getStatus().equals(Constants.STATUS_OFFLINE)) { // wake on lan only when offline
                    try {
                        sendMagicPacket(dev.getMacAddress(), Constants.LAB_BROADCAST_IP);
                        Log.d("WakeOnLan", "Magic packet sent to " + dev.getNetworkName());
                    } catch (Exception e) {
                        Log.e("WakeOnLan", "Failed to send magic packet to " + dev.getNetworkName(), e);
                    }
                }
            });
        }

        lock.release();

        new Thread(() -> { // new thread to not block main one
            try {
                latch.await();  // wait until all wake packets sent (all wol threads to finish)
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // restore interrupted status
            }
            // restore broadcast (multicast lock) to being disable
            lock.release();
        }).start();
    }

    private static void sendMagicPacket(String macAddress, String broadcastIp) throws Exception {
        Log.d("WakeOnLAN", "Preparing to send magic packet");

        // prepare packet's context
        byte[] macBytes = getMacBytes(macAddress);
        byte[] packet = new byte[6 + 16 * macBytes.length];

        for (int i = 0; i < 6; i++) {
            packet[i] = (byte) 0xFF;
        }

        for (int i = 6; i < packet.length; i += macBytes.length) {
            System.arraycopy(macBytes, 0, packet, i, macBytes.length);
        }

        // prepare wol packet
        InetAddress address = InetAddress.getByName(broadcastIp);
        DatagramPacket datagramPacket = new DatagramPacket(packet, packet.length, address, Constants.WOL_PORT);
        DatagramSocket datagramSocket = new DatagramSocket();
        datagramSocket.setBroadcast(true);
        datagramSocket.send(datagramPacket);
        datagramSocket.close();
    }

    private static byte[] getMacBytes(String macStr) throws IllegalArgumentException {
        byte[] bytes = new byte[6];
        String[] hex = macStr.split("[:-]");
        if (hex.length != 6) {
            throw new IllegalArgumentException("Invalid MAC address format: " + macStr);
        }
        try {
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) Integer.parseInt(hex[i], 16);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid hex digit in MAC address.");
        }
        return bytes;
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
