package com.example.labcontrolapp;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;

public class MainActivity extends AppCompatActivity {

    MaterialToolbar toolbar;
    private SocketCommunication client;


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

        Device pc1 = new Device("PC1", "Win", true, "777");

//        client = new SocketCommunication(this);
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                client.connect();
//            }
//        }).start();
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