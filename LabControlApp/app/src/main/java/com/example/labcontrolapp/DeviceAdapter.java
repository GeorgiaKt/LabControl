package com.example.labcontrolapp;

import android.content.Context;
import android.view.HapticFeedbackConstants;
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
    private Context context;
    private OnDeviceClickListener listener;

    public DeviceAdapter() {
        this.devList = new ArrayList<>();
    }

    public void attachToAdapter(ArrayList<Device> devicesList, Context ctx, OnDeviceClickListener listener) {
        this.devList.clear();
        this.devList.addAll(devicesList);
        this.context = ctx;
        this.listener = listener; // reference to Main Activity's listener
        notifyDataSetChanged();
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
        holder.networkNameText.setText(device.getNetworkName());
        holder.nameText.setText(device.getName());
        holder.osText.setText(device.getOs());
        holder.statusText.setText(device.getStatus());
        holder.computerImageView.setImageResource(R.drawable.computer_1); // default computer icon
        holder.checkmarkImageView.setImageResource(R.drawable.black_round_checkmark);

        // change status icon based on data
        if (Constants.STATUS_ONLINE.equalsIgnoreCase(device.getStatus()))
            holder.statusImageView.setImageResource(R.drawable.green_circle);
        else if (Constants.STATUS_OFFLINE.equalsIgnoreCase(device.getStatus()))
            holder.statusImageView.setImageResource(R.drawable.red_circle);
        else
            holder.statusImageView.setImageResource(R.drawable.black_circle); // default status icon (black)

        // change visibility of the checkmark image based on selection
        if (devList.get(position).isSelected())
            holder.checkmarkImageView.setVisibility(View.VISIBLE);
        else
            holder.checkmarkImageView.setVisibility(View.GONE);


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) { // proceed only if View Holder's position is still valid
                    listener.onDeviceClickListener(currentPosition);
                }
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() { // handle long click
            @Override
            public boolean onLongClick(View view) {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) { // proceed only if View Holder's position is still valid
                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    listener.onDeviceLongClickListener(currentPosition);
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return devList.size();
    }


    public static class DeviceViewHolder extends RecyclerView.ViewHolder {
        TextView networkNameText, nameText, osText, statusText;
        ImageView computerImageView, statusImageView, checkmarkImageView;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            // references to all views in each card
            networkNameText = itemView.findViewById(R.id.networkNameText);
            nameText = itemView.findViewById(R.id.nameText);
            osText = itemView.findViewById(R.id.osText);
            statusText = itemView.findViewById(R.id.statusText);
            computerImageView = itemView.findViewById(R.id.computerImageView);
            statusImageView = itemView.findViewById(R.id.statusImageView);
            checkmarkImageView = itemView.findViewById(R.id.checkmarkImageView);
        }

    }
}
