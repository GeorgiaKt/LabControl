package com.example.labcontrolapp;

import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;


public class MainActivity extends AppCompatActivity implements OnDeviceClickListener {
    MaterialToolbar toolbar;
    RecyclerView recyclerView;
    DeviceAdapter deviceAdapter;
    DeviceManager deviceManager;
    ProgressBar progressBar;
    ActionMode actionMode;

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
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);

        setSupportActionBar(toolbar);
        progressBar.setVisibility(View.VISIBLE); // make progress bar visible before connection
        recyclerView.setEnabled(false); // disable interactions while loading

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columns
        recyclerView.setHasFixedSize(true); // recycler view stays the same size

        deviceManager = new DeviceManager(this);
        deviceManager.initializeDevices();

        deviceAdapter = new DeviceAdapter(deviceManager.getDevicesList(), this, this);
        recyclerView.setAdapter(deviceAdapter);

        deviceManager.connectDevices(deviceAdapter, () -> {
            progressBar.setVisibility(View.GONE); // hide progress bar when all devices connect (successfully or not)
            recyclerView.setEnabled(true); // enable interactions
        });

    }

    ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {

        }
    };


    @Override
    protected void onDestroy() {
        deviceManager.disconnectDevices();
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

    @Override
    public void onDeviceLongClickListener(int position) {
        if (actionMode == null) { // if there isn't
            actionMode = startActionMode(actionModeCallback);
        }
        boolean sel = deviceManager.getDevicesList().get(position).isSelected();
        deviceManager.getDevicesList().get(position).setSelected(!sel); // toggle selection of the device
        deviceAdapter.notifyDataSetChanged(); // update ui
    }
}