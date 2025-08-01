package com.example.labcontrolapp;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.ActionMode;
import android.Manifest;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;


public class MainActivity extends AppCompatActivity implements OnDeviceClickListener, NetworkMonitor.NetworkStateListener {
    // ui components
    MaterialToolbar toolbar;
    RecyclerView recyclerView;
    ProgressBar progressBar;
    View blockingOverlay; // overlay for blocking interactions while loading
    ImageButton responsesButton;
    // core components
    DeviceManager deviceManager;
    DeviceAdapter deviceAdapter;
    ActionMode actionMode; // for multiple item selection
    NetworkMonitor networkMonitor;
    EchoService echoService;
    ResponsesBottomSheet responseBottomSheet;
    boolean isBound = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private Dialog settingsDialog, explanationDialog;
    // booleans needed for managing location permission dialogs
    private boolean appStarted = false;
    private boolean deniedPermanentlyPermission = false;

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

        toolbar = findViewById(R.id.materialToolbar);
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        blockingOverlay = findViewById(R.id.blockingOverlay);
        responsesButton = findViewById(R.id.responsesButton);

        setSupportActionBar(toolbar);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columns
        recyclerView.setHasFixedSize(true); // recycler view stays the same size

        responseBottomSheet = new ResponsesBottomSheet();

        deviceManager = new DeviceManager(this);
        deviceAdapter = new DeviceAdapter();
        deviceManager.attachDeviceAdapter(deviceAdapter);
        recyclerView.setAdapter(deviceAdapter);

        responsesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                responseBottomSheet.show(getSupportFragmentManager(), "ModalBottomSheet");
            }
        });

        if (!hasLocationPermission())
            requestLocationPermission();

    }

    @Override
    protected void onResume() {
        super.onResume();
        checkLocationPermission();
    }

    @Override
    protected void onStop() {
        if (isBound) {
            echoService.stopEchoing();
            unbindService(serviceConnection);
            isBound = false;
        }
        if (networkMonitor != null) {
            networkMonitor.stop();
            networkMonitor = null;
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        // dismiss any visible dialogs
        if (settingsDialog != null && settingsDialog.isShowing())
            settingsDialog.dismiss();
        if (explanationDialog != null && explanationDialog.isShowing())
            explanationDialog.dismiss();
        // clean resources
        deviceManager.shutdownExecutors();
        super.onDestroy();
    }

    private boolean hasLocationPermission() { // check if fine location permission has already been granted
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("LocationPermission", "Permission Granted");
            } else {
                // location permission denied
                if (!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    // denied permanently — guide user to settings
                    deniedPermanentlyPermission = true;
                    showSettingsDialog();
                    Log.d("LocationPermission", "Permission Denied Permanently");
                } else {
                    // denied temporarily — explain again
                    showExplanationDialog();
                    Log.d("LocationPermission", "Permission Denied Temporarily");
                }
            }
        }
    }

    private void showSettingsDialog() {
        if (settingsDialog == null || !settingsDialog.isShowing())
            settingsDialog = new MaterialAlertDialogBuilder(this)
                    .setTitle("Permission Required")
                    .setMessage("Precise location permission is permanently denied. Please enable it from app settings.")
                    .setCancelable(false)
                    .setPositiveButton("Open Settings", (dialog, which) -> { // redirect to app's info settings screen, to change permission
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    })
                    .setNegativeButton("Exit App", (dialog, which) -> finish())
                    .show();
    }

    private void showExplanationDialog() {
        if (explanationDialog == null || !explanationDialog.isShowing())
            explanationDialog = new MaterialAlertDialogBuilder(this)
                    .setTitle("Location Permission Needed")
                    .setMessage("Lab Control needs location permission to detect the lab Wi-Fi network.")
                    .setCancelable(false)
                    .setPositiveButton("Try Again", (dialog, which) -> {
                        requestLocationPermission();
                    })
                    .setNegativeButton("Exit App", (dialog, which) -> finish())
                    .show();
    }

    private void checkNetworkMonitor() {
        if (networkMonitor == null) {
            networkMonitor = new NetworkMonitor(this, this, this);
            networkMonitor.start();
        } else {
            networkMonitor.handleNetworkState();
        }
    }

    private void checkLocationPermission() {
        // check if location permission is granted
        if (hasLocationPermission()) {
            if (!appStarted) { // start network monitoring if it hasn't started, then start app if connection is valid
                startLoading();
                deniedPermanentlyPermission = false;
            }
            checkNetworkMonitor();
        } else { // permission not granted
            if (!appStarted)
                if (deniedPermanentlyPermission) // if user denied permanently permission to location and app hasn't started
                    showSettingsDialog();
        }
    }

    private void startApp() {
        Log.d("MainActivity", "Starting app...");
        deviceManager.initializeDevices();
        appStarted = true;
        deviceAdapter.attachToAdapter(deviceManager.getDevicesList(), this, this);
        stopLoading();
    }

    private void startLoading() {
        progressBar.setVisibility(View.VISIBLE); // make progress bar visible before connection
        blockingOverlay.setVisibility(View.VISIBLE); // block interactions
    }

    private void stopLoading() {
        progressBar.setVisibility(View.GONE); // hide progress bar when all devices connect (successfully or not)
        blockingOverlay.setVisibility(View.GONE); // allow interactions
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
                deviceManager.handleWakeOnLan();
                actionMode.finish();
                return true;
            } else if (menuItem.getItemId() == R.id.selectAllMenuItem) {
                // select/deselect all devices
                if (deviceManager.areAllSelected())
                    deviceManager.clearSelection();
                else
                    deviceManager.selectAll();
                updateContextualBarTitle();
                deviceAdapter.notifyDataSetChanged();
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

    public void updateContextualBarTitle() {
        // add as a title of the cab the number of selected devices
        int selectedCount = deviceManager.getSelectedDevices().size();
        actionMode.setTitle(selectedCount + "");
    }

    public void appendResponseToBottomSheet(String newResponses) { // update the responses list in the bottom sheet dynamically
        if (responseBottomSheet != null) {
            responseBottomSheet.appendResponse(newResponses);
        }
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
    public void onDeviceClickListener(int position) {
        if (actionMode != null) { // if contextual action bar is activated
            deviceManager.toggleSelection(position);
            deviceAdapter.notifyItemChanged(position);
            updateContextualBarTitle();
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
    }

    @Override
    public void onNetworkAvailable() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("MainActivity", "Network & Location available");
                // start app after ensuring location permission is granted, location services are enabled and device is connected to lab's LAN
                if (!appStarted) // 1st time
                    startApp();

                // start echo service after app and network monitor have started
                bindEchoService();
            }
        });
    }

    private void bindEchoService() {
        if (!isBound) {
            Intent intent = new Intent(MainActivity.this, EchoService.class);
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
    }


    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d("MainActivityEchoService", "Connected to Echo Service");
            EchoService.LocalBinder binder = (EchoService.LocalBinder) iBinder;
            echoService = binder.getService();
            echoService.setDeviceManager(deviceManager);
            isBound = true;
            echoService.startEchoing();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isBound = false;
        }
    };

}