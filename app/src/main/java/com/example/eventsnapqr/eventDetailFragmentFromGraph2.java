package com.example.eventsnapqr;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

public class eventDetailFragmentFromGraph2 extends Fragment {
    public eventDetailFragmentFromGraph2() {
        // Required empty public constructor
    }

    private String eventName;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventName = getArguments().getString("eventName");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_details, container, false);
        view.findViewById(R.id.button_back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().onBackPressed();
            }
        });

        view.findViewById(R.id.sign_up_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateDialog(eventName);
            }
        });

        return view;
    }

    public void CreateDialog(String eventName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("You have successfully signed up for " + eventName)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Back to main page
                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        startActivity(intent);
                    }
                });

        builder.create().show();
    }
}
