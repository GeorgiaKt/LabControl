package com.example.labcontrolapp;

import static androidx.core.location.LocationManagerCompat.isLocationEnabled;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class NetworkMonitor {
    private Context context;
    private ConnectivityManager connectivityManager;
    private NetworkRequest networkRequest;
    private ConnectivityManager.NetworkCallback networkCallback;
    private final NetworkStateListener listener;
    private final MainActivity mainActivity;
    private Dialog locationDialog;
    private Dialog labDialog;
    // flags to check network and location status
    private boolean isEthernet = false;
    private boolean isWiFi = false;
    private boolean locationEnabled = false;
    private boolean newConnection = false;
    private boolean wasConnected = false;


    // listener to notify app about connectivity changes
    public interface NetworkStateListener {
        void onNetworkAvailable();

        void onNetworkUnavailable();
    }

    public NetworkMonitor(MainActivity mainActivity, Context context, NetworkStateListener listener) {
        this.mainActivity = mainActivity;
        this.context = context;
        this.listener = listener;
        connectivityManager = context.getSystemService(ConnectivityManager.class);
    }

    public void start() {
        // don't start if connectivity manager hasn't been initialized or monitoring has already started
        if (connectivityManager == null || networkCallback != null)
            return;

        Log.d("NetworkMonitor", "Network Monitor Started");

        networkRequest = new NetworkRequest.Builder() // type of networks want to listen to: internet capability via wifi/ethernet
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
                .build();

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                Log.d("NetworkMonitor", "New Network Available");
                newConnection = true;
                handleNetworkState();
            }

            @Override
            public void onLost(@NonNull Network network) {
                super.onLost(network);
                handleNetworkState();
            }

            @Override
            public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
                super.onCapabilitiesChanged(network, networkCapabilities);
                handleNetworkState();
            }
        };

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
        handleNetworkState();
    }

    public void stop() {
        if (connectivityManager != null && networkCallback != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
            networkCallback = null;
            Log.d("NetworkMonitor", "Network Monitor Stopped");
        }
    }

    public void handleNetworkState() {
        // check location & network
        checkLocationState();
        checkNetworkState();

        boolean isConnected = isEthernet || isWiFi;

        // show dialog based on location and network status
        if (!locationEnabled) {
            dismissDialog(labDialog); // close lab dialog if shown
            showLocationDialog(); // show alert dialog prompting user to turn on location services
        } else {
            dismissDialog(locationDialog); // close location dialog if shown
            if (!isConnected) // if not connected to lab's LAN
                showLabDialog(); // show alert dialog prompting user to check wifi network
            else
                dismissDialog(labDialog);
        }

        if (!wasConnected && isConnected) { // if there was a connection change - from disconnected to connected
            if (newConnection) { // new connection
                Log.d("NetworkMonitor", "Connected to New Network");
                newConnection = false;
                listener.onNetworkAvailable();
            }
        }
        wasConnected = isConnected;
    }

    private void dismissDialog(Dialog dialog) {
        if (dialog != null && dialog.isShowing())
            dialog.dismiss();
    }

    private void checkLocationState() {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            if (!isLocationEnabled(locationManager)) { // if location is disabled
                Log.d("NetworkMonitor", "Location Services Disabled");
                locationEnabled = false;
            } else {
                Log.d("NetworkMonitor", "Location Services Enabled");
                locationEnabled = true;
            }
        }
    }

    private void showLocationDialog() {
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!mainActivity.isFinishing() && !mainActivity.isDestroyed()) {
                    if (locationDialog == null || !locationDialog.isShowing()) // if no new dialog instance exists or exists but is not visible
                        locationDialog = new MaterialAlertDialogBuilder(mainActivity)
                                .setTitle("Enable Location")
                                .setMessage("Location services are required to detect the lab Wi-Fi network.")
                                .setCancelable(false)
                                .setPositiveButton("Open Settings", (dialog, which) -> {
                                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    mainActivity.startActivity(intent);
                                })
                                .show();
                }
            }
        });
    }

    private void checkNetworkState() {
        isWiFi = false;
        isEthernet = false;
        Network activeNetwork = connectivityManager.getActiveNetwork();
        if (activeNetwork != null) {
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
            // check if internet is available and working & is connected via wifi or ethernet
            if (capabilities != null &&
                    (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    Log.d("NetworkMonitor", "Connected via Ethernet");
                    isEthernet = true;
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    Log.d("NetworkMonitor", "Connected via WiFi");
                    if (isConnectedToLabWiFi()) {
                        Log.d("NetworkMonitor", "Connected to Lab's WiFi");
                        isWiFi = true;
                    }
                }
            }
        }
    }

    private boolean isConnectedToLabWiFi() {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null) {
                if (locationEnabled) {
                    String ssid = wifiInfo.getSSID();
                    Log.d("NetworkMonitor", "SSID: " + ssid);
                    if (ssid != null && ssid.equals(Constants.LAB_SSID))
                        return true;
                }
            }
        }
        return false;
    }

    private void showLabDialog() {
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!mainActivity.isFinishing() && !mainActivity.isDestroyed()) {
                    if (labDialog == null || !labDialog.isShowing()) // if no new dialog instance exists or exists but is not visible
                        labDialog = new MaterialAlertDialogBuilder(mainActivity)
                                .setTitle("Lab Network Required")
                                .setMessage("Lab Control only works when connected to the lab's network. Ensure you are on the correct LAN (via Wi-Fi or Ethernet).")
                                .setCancelable(false)
                                .setPositiveButton("Open Settings", (dialog, which) -> {
                                    Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                                    mainActivity.startActivity(intent);
                                })
                                .show();
                }
            }
        });
    }
}
