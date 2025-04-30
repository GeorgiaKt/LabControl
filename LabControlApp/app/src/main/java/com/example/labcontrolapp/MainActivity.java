package com.example.labcontrolapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.materialswitch.MaterialSwitch;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    MaterialToolbar toolbar;
    MaterialSwitch switchConnection;

    private SocketClient client;


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

        toolbar = (MaterialToolbar) findViewById(R.id.materialToolbar);

        switchConnection = (MaterialSwitch) findViewById(R.id.SwitchConnection);

        switchConnection.setOnClickListener(this);

        setSupportActionBar(toolbar);

        client = new SocketClient(this);
    }

    @Override
    public void onClick(View v) {
        if (v == switchConnection)
            if (switchConnection.isChecked()) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        client.connect();
                    }
                }).start();
            } else
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        client.disconnect();
                    }
                }).start();

    }

    @Override
    protected void onDestroy() {
        if (client !=  null) {
            client.disconnect();
            client = null;
        }
        super.onDestroy();
    }


    public void displayToast(String s){
        if (!isFinishing() && !isDestroyed()) // check if application is still running
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast toast = Toast.makeText (getApplicationContext (), s,
                            Toast.LENGTH_SHORT);
                    toast.show ();
                }
            });
    }

}