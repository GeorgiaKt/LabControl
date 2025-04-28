package com.example.labcontrolapp;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Toolbar toolbar;
    Switch switchConnection;

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

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        switchConnection = (Switch) findViewById(R.id.SwitchConnection);

        switchConnection.setOnClickListener(this);

        setSupportActionBar(toolbar);

        client = new SocketClient();
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

    public void displayToast(String s){
        Toast toast = Toast.makeText (getApplicationContext (), s,
                Toast.LENGTH_SHORT);
        toast.setGravity (Gravity.BOTTOM, 0, 0);
        toast.show ();
    }

}