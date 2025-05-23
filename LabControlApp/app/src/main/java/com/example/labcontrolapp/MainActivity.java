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


public class MainActivity extends AppCompatActivity implements OnDeviceClickListener, OnDevicesCallback, NetworkMonitor.NetworkStateListener {
    // ui components
    MaterialToolbar toolbar;
    RecyclerView recyclerView;
    ProgressBar progressBar;
    // core components
    DeviceManager deviceManager;
    DeviceAdapter deviceAdapter;
    ActionMode actionMode; // for multiple item selection
    View blockingOverlay; // overlay for blocking interactions while loading
    NetworkMonitor networkMonitor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this); // edge to edge layout

        setContentView(R.layout.main_activity_layout);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        networkMonitor = new NetworkMonitor(this, this);
        networkMonitor.start();


        toolbar = findViewById(R.id.materialToolbar);
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        blockingOverlay = findViewById(R.id.blockingOverlay);

        setSupportActionBar(toolbar);
        progressBar.setVisibility(View.VISIBLE); // make progress bar visible before connection
        blockingOverlay.setVisibility(View.VISIBLE); // block interactions

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columns
        recyclerView.setHasFixedSize(true); // recycler view stays the same size

        deviceManager = new DeviceManager(this);
        deviceManager.initializeDevices();

        deviceAdapter = new DeviceAdapter(deviceManager.getDevicesList(), this, this);
        recyclerView.setAdapter(deviceAdapter);

        deviceManager.connectDevices(deviceAdapter, this);

    }

    @Override
    protected void onDestroy() {
        deviceManager.shutdownExecutors();
        deviceManager.disconnectDevices();
        super.onDestroy();
        if (networkMonitor != null) {
            networkMonitor.stop();
        }
    }

    // callback for contextual action bar (multi-selection mode)
    ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            actionMode.getMenuInflater().inflate(R.menu.multiple_selection_bar_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            if (menuItem.getItemId() == R.id.echoMenuItem) { // echo
                deviceManager.handleMessageExchange(Constants.COMMAND_ECHO);
                actionMode.finish();
                return true;
            } else if (menuItem.getItemId() == R.id.restartMenuItem) {
                deviceManager.handleMessageExchange(Constants.COMMAND_RESTART);
                actionMode.finish();
                return true;
            } else if (menuItem.getItemId() == R.id.shutDownMenuItem) {
                deviceManager.handleMessageExchange(Constants.COMMAND_SHUTDOWN);
                actionMode.finish();
                return true;
            } else if (menuItem.getItemId() == R.id.restoreMenuItem) {
                deviceManager.handleMessageExchange(Constants.COMMAND_RESTORE);
                actionMode.finish();
                return true;
            } else if (menuItem.getItemId() == R.id.wakeMenuItem) {

                actionMode.finish();
                return true;
            }

            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
            deviceManager.clearSelection();
            deviceAdapter.notifyDataSetChanged();
        }
    };


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
    public void onDeviceClickListener(int position) {
        if (actionMode != null) { // if contextual action bar is activated
            deviceManager.toggleSelection(position);
            deviceAdapter.notifyItemChanged(position);
            updateContextualBarTitle();

            // end action mode if no items are selected
            if (deviceManager.getSelectedDevices().isEmpty()) {
                actionMode.finish();
            }
        }
    }

    @Override
    public void onDeviceLongClickListener(int position) { // called when a device is long-pressed
        if (actionMode == null) { // if there isn't a contextual action bar already activated
            actionMode = startActionMode(actionModeCallback);
        }
        deviceManager.toggleSelection(position);
        deviceAdapter.notifyItemChanged(position);
        updateContextualBarTitle();

        // end action mode if no items are selected
        if (deviceManager.getSelectedDevices().isEmpty()) {
            actionMode.finish();
        }
    }

    @Override
    public void onAllDevicesConnected() { // called when all threads for device connecting finish
        progressBar.setVisibility(View.GONE); // hide progress bar when all devices connect (successfully or not)
        blockingOverlay.setVisibility(View.GONE); // allow interactions
    }


    public void updateContextualBarTitle() {
        // add as a title of the cab the number of selected devices
        int selectedCount = deviceManager.getSelectedDevices().size();
        actionMode.setTitle(selectedCount + "");
    }

    @Override
    public void onNetworkAvailable() {
        displayToast("Connected");
    }

    @Override
    public void onNetworkUnavailable() {
        displayToast("Disconnected");
    }
}