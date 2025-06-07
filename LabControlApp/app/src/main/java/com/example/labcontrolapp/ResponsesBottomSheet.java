package com.example.labcontrolapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

public class ResponsesBottomSheet extends BottomSheetDialogFragment {
    TextView responeseTextView;
    ArrayList<String> responsesList = new ArrayList<>();


    public ArrayList<String> getResponsesList() {
        return responsesList;
    }

    public void setResponsesList(ArrayList<String> responsesList) {
        this.responsesList = responsesList;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // inflate the bottom sheet layout
        View view = inflater.inflate(R.layout.bottom_sheet_layout, container, false);
        responeseTextView = view.findViewById(R.id.responsesTextView);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        displayResponses();
    }

    @Override
    public void onStart() {
        super.onStart();

        View view = getView();
        if (view != null) {
            View parent = (View) view.getParent();
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(parent);

            parent.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
            parent.setLayoutParams(parent.getLayoutParams());

            behavior.setHideable(true);
            behavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
            behavior.setFitToContents(true);
        }
    }


    public void displayResponses() {
        if (!responsesList.isEmpty()) {
            String tmpText = "";
            for (String response : responsesList) {
                tmpText = tmpText + "\n" + "> " + response;
            }
            responeseTextView.setText(tmpText);
        }
    }
}
