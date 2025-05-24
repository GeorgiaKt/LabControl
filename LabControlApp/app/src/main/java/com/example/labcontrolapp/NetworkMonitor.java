package com.example.labcontrolapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import androidx.annotation.NonNull;

public class NetworkMonitor {
    private Context context;
    private ConnectivityManager connectivityManager;

    private ConnectivityManager.NetworkCallback networkCallback;
    private boolean isEthernet = false;
    private boolean isWiFi = false;
    private final NetworkStateListener listener;

    // listener to notify app about connectivity changes
    public interface NetworkStateListener {
        void onNetworkAvailable();

        void onNetworkUnavailable();
    }

    public NetworkMonitor(Context context, NetworkStateListener listener) {
        this.context = context;
        this.listener = listener;
        connectivityManager = context.getSystemService(ConnectivityManager.class);
    }

    public void start() {
        if (connectivityManager == null)
            return;

        NetworkRequest networkRequest = new NetworkRequest.Builder() // type of networks want to listen to: internet capability via wifi/ethernet
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
                .build();

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
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
        }
    }


    private void handleNetworkState() {
        // previous state
        boolean wasEthernet = isEthernet;
        boolean wasWiFi = isWiFi;

        checkNetworkState();

        boolean wasConnected = wasEthernet || wasWiFi;
        boolean isConnected = isEthernet || isWiFi;
        if (isConnected != wasConnected) // if there is a connectivity change
            if (isConnected)
                listener.onNetworkAvailable();
            else
                listener.onNetworkUnavailable();

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
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
                    isEthernet = true;
                else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    isConnectedToLabWiFi();
                    isWiFi = true;
                }
            }

        }
    }

    private void isConnectedToLabWiFi() {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null){
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo !=null){
                String ssid = wifiInfo.getSSID();
                Log.d("NetworkMonitor", "SSID: " + ssid);
            }

        }
    }
}
