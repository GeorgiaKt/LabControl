package com.example.labcontrolapp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;


public class EchoService extends Service {


    private final IBinder Binder = new LocalBinder ();

    public class LocalBinder extends android.os.Binder
    {
        EchoService getService ()
        {
            return EchoService.this;
        }
    }

    @Override
    public IBinder onBind (Intent intent)
    {
        Log.d("EchoService", "Bound");
        return Binder;
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        Log.d("EchoService", "Unbounded");
        return false;
    }

}
