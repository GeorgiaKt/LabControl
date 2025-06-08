package com.example.labcontrolapp;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

public class ResponsesBottomSheet extends BottomSheetDialogFragment {
    LinearLayout responsesContainer;
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
        responsesContainer = view.findViewById(R.id.responsesContainer);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) { // called after onCreatView
        super.onViewCreated(view, savedInstanceState);
        displayResponses(); // update responses on bottom sheet
    }

    @Override
    public void onStart() {
        super.onStart();

        View view = getView();
        if (view != null) {
            View parent = (View) view.getParent();
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(parent);

            // adjust bottom sheet's behavior only if there are responses
            if (!responsesList.isEmpty()) {
                // allow bottom sheet to take full height when fully expanded
                parent.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                parent.setLayoutParams(parent.getLayoutParams());

                behavior.setHideable(true);
                behavior.setPeekHeight(500); // max collapsed height
                behavior.setState(BottomSheetBehavior.STATE_COLLAPSED); // initial state
            }
        }
    }


    public void displayResponses() {
        // set left padding of the text based on the screen size
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        // change left padding based on response list state (if its empty or not)

        if (responsesContainer == null) return;

        if (responsesList.isEmpty()) { // when no responses, show corresponding text
            int leftPadding = (int) (0.2 * screenWidth); // 20% of screen width

            TextView responseTextView = new TextView(getContext());
            responseTextView.setText("No responses from devices !");
            responseTextView.setTextSize(16);
            responseTextView.setPadding(leftPadding, 15, 0, 8);
            responseTextView.setTypeface(null, Typeface.ITALIC);

            responsesContainer.addView(responseTextView);
            return;
        }

        // when there are responses from devices
        responsesContainer.removeAllViews(); // clear existing views if any

        int leftPadding = (int) (0.12 * screenWidth); // 12% of screen width

        for (String response : responsesList) { // for each response, create a text view
            TextView responseTextView = new TextView(getContext());
            responseTextView.setText("‣ " + response);
            responseTextView.setTextSize(16);
            responseTextView.setPadding(leftPadding, 8, 0, 8);

            responsesContainer.addView(responseTextView);
        }
    }
}
