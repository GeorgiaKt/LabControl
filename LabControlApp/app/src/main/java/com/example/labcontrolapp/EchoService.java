package com.example.labcontrolapp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class EchoService extends Service {
    private DeviceManager deviceManager;
    private boolean isEchoing = false;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();


    public void setDeviceManager(DeviceManager deviceManager) {
        this.deviceManager = deviceManager;
    }

    public void startEchoing() {
        Log.d("EchoService", "Start Echoing");
        if (isEchoing)
            return;
        isEchoing = true;
        scheduler.scheduleWithFixedDelay(() -> {
            Log.d("EchoService", "ECHO");
            deviceManager.echoAllDevices();
        }, 0, 2, TimeUnit.MINUTES); // send echo to all devices every 2 minutes (from the time the previous server's response for echo got received)

    }

    public void stopEchoing() {
        Log.d("EchoService", "Stop Echoing");
        isEchoing = false;
        if (scheduler != null && !scheduler.isShutdown())
            scheduler.shutdown();
    }

    private final IBinder Binder = new LocalBinder();

    public class LocalBinder extends android.os.Binder {
        EchoService getService() {
            return EchoService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("EchoService", "Bound");
        return Binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("EchoService", "Unbounded");
        return false;
    }

}
