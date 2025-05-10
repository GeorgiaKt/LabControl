package com.example.labcontrolapp;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    MaterialToolbar toolbar;
    RecyclerView recyclerView;
    ArrayList<Device> devicesList;
    DeviceAdapter deviceAdapter;


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

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columns
        recyclerView.setHasFixedSize(true); // recycler view stays the same size

        initializeDevices();
        connectDevices();

        deviceAdapter = new DeviceAdapter(devicesList, this);
        recyclerView.setAdapter(deviceAdapter);

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

    private void connectDevices() {
        for (int i = 0; i < devicesList.size(); i++) {
            final int index = i;
            Device dev = devicesList.get(i);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean connectionResult = dev.getClient().connect();
                    if (connectionResult)
                        dev.setStatus("Online");
                    else
                        dev.setStatus("Offline");


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            deviceAdapter.notifyItemChanged(index);
                        }
                    });
                }
            }).start();

        }

    }

    @Override
    protected void onDestroy() {
        // close socket connection if still connected
        if (devicesList != null){
            for (Device dev : devicesList){
                if (dev.getClient() != null) {
                    dev.getClient().disconnect();
                    dev.setClient(null);
                }
            }
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