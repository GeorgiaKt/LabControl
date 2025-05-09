package com.example.labcontrolapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {
    ArrayList<Device> devList;
    private Context context;

    public DeviceAdapter(ArrayList<Device> devicesList, Context ctx) {
        this.devList = new ArrayList<>(devicesList);
        this.context = ctx;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.device_item_layout, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        Device device = devList.get(position);

        holder.nameText.setText(device.getName());
        holder.osText.setText(device.getOs());
        holder.statusText.setText(device.getStatus());
        holder.computerImageView.setImageResource(R.drawable.computer_1);
        holder.statusImageView.setImageResource(R.drawable.gray_circle);

        if ("Online".equalsIgnoreCase(device.getStatus())) {
            holder.statusImageView.setImageResource(R.drawable.green_circle);
        } else if ("Offline".equalsIgnoreCase(device.getStatus())) {
            holder.statusImageView.setImageResource(R.drawable.red_circle);
        }
    }

    @Override
    public int getItemCount() {
        return devList.size();
    }


    public static class DeviceViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, osText, statusText;
        ImageView computerImageView, statusImageView;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.nameText);
            osText = itemView.findViewById(R.id.osText);
            statusText = itemView.findViewById(R.id.statusText);
            computerImageView = itemView.findViewById(R.id.computerImageView);
            statusImageView = itemView.findViewById(R.id.statusImageView);
        }
    }
}
