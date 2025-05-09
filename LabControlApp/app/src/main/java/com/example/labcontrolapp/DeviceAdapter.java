package com.example.labcontrolapp;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {
    ArrayList<Device> devList;
    private Context ctx;

    public DeviceAdapter(ArrayList<Device> devicesList, Context ctx) {
        this.devList = devicesList;
        this.ctx = ctx;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }


    public static class DeviceViewHolder extends RecyclerView.ViewHolder{

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
