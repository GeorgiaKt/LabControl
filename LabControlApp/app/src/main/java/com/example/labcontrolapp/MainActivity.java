package com.example.labcontrolapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    MaterialToolbar toolbar;
    private SocketCommunication client;
    ArrayList<Device> devicesList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        toolbar = findViewById(R.id.materialToolbar);
        setSupportActionBar(toolbar);

        initializeDevices();

//        client = new SocketCommunication(this, "10.0.2.2"); // server's IP
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                client.connect();
//            }
//        }).start();
    }

    private void initializeDevices() {
        devicesList = new ArrayList<>();
        for (int i = 1; i < 28; i++) {
            if (i < 10) {
                devicesList.add(new Device("PRPC0" + i, "192.168.88." + i, "245:34:1C:4T:" + i));
            } else {
                devicesList.add(new Device("PRPC" + i, "192.168.88." + i, "245:34:1C:4T:" + i));
            }
        }
    }

    @Override
    protected void onDestroy() {
        // close socket connection if still connected
        if (client != null) {
            client.disconnect();
            client = null;
        }
        super.onDestroy();
    }


    public void displayToast(String s) {
        if (!isFinishing() && !isDestroyed()) // check if application is still running
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast toast = Toast.makeText(getApplicationContext(), s,
                            Toast.LENGTH_SHORT);
                    toast.show();
                }
            });
    }

}