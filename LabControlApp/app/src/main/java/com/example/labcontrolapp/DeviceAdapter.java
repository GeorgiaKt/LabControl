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
    ArrayList<Device> devList; // list of displayed devices
    private final Context context;

    public DeviceAdapter(ArrayList<Device> devicesList, Context ctx) {
        this.devList = new ArrayList<>(devicesList);
        this.context = ctx;
    }

    public void updateList(@NonNull ArrayList<Device> newList) {
        // check for any changed devices and update it
        for (int i = 0; i < newList.size(); i++) {
            Device newDevice = newList.get(i);
            if (!newDevice.equals(devList.get(i))) {
                devList.set(i, newDevice); // replace changed device
                notifyItemChanged(i);      // refresh just that item
            }
        }
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflate layout of a single device card
        View view = LayoutInflater.from(context).inflate(R.layout.device_item_layout, parent, false);
        return new DeviceViewHolder(view); // return ViewHolder object that holds references to the views inside the layout
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        // bind data to views
        Device device = devList.get(position);
        // display data
        holder.nameText.setText(device.getName());
        holder.osText.setText(device.getOs());
        holder.statusText.setText(device.getStatus());
        holder.computerImageView.setImageResource(R.drawable.computer_1); // default computer icon
        holder.statusImageView.setImageResource(R.drawable.gray_circle); // default status icon (gray)

        // change status icon based on data
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
            // references to all views in each card
            nameText = itemView.findViewById(R.id.nameText);
            osText = itemView.findViewById(R.id.osText);
            statusText = itemView.findViewById(R.id.statusText);
            computerImageView = itemView.findViewById(R.id.computerImageView);
            statusImageView = itemView.findViewById(R.id.statusImageView);
        }
    }
}
