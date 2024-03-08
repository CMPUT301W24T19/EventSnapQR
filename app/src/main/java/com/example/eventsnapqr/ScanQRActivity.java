package com.example.eventsnapqr;

import static androidx.core.content.ContentProviderCompat.requireContext;
import static androidx.fragment.app.FragmentManager.TAG;
import static java.security.AccessController.getContext;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import android.Manifest;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

public class ScanQRActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CAMERA = 1;

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
            } else {
                initQRCodeScanner();
            }
        } else {
            initQRCodeScanner();
        }
    }


    private void initQRCodeScanner() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setOrientationLocked(false);
        integrator.setPrompt("Scan QR Code");
        integrator.setCameraId(0);
        integrator.initiateScan();
    }

    private void notSignedUpDialog(String eventId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ScanQRActivity.this);
        builder.setTitle("Not Signed-Up for Event")
                .setPositiveButton("View Event Details", (dialog, which) -> {
                    NavController navController = Navigation.findNavController(ScanQRActivity.this, R.id.nav_host_fragment);
                    Bundle bundle = new Bundle();
                    bundle.putString("eventId", eventId);
                    navController.navigate(R.id.eventDetailsFragment, bundle);
                })
                .setNegativeButton("Return", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Back to main page
                        NavController navController = Navigation.findNavController(ScanQRActivity.this, R.id.nav_host_fragment);
                        navController.navigate(R.id.mainPageFragment);
                    }
                })
                .create()
                .show();
    }


    private void checkIn(String eventID) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ScanQRActivity.this);
        builder.setMessage("You have successfully checked into " + eventID)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Back to main page
                        NavController navController = Navigation.findNavController(ScanQRActivity.this, R.id.nav_host_fragment);
                        navController.navigate(R.id.mainPageFragment);
                    }
                });

        builder.create().show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (intentResult != null) {
            String contents = intentResult.getContents();
            if (contents != null) {
                String eventId = contents;
                FirebaseController.getInstance().checkUserInAttendees(eventId, userId, new FirebaseController.OnUserInAttendeesListener() {
                    @Override
                    public void onUserInAttendees(boolean isInAttendees) {
                        if (isInAttendees) {
                            // make new fragment that says you have successfully checked into event
                            // increment
                            checkIn(eventId);
                        } else {
                            notSignedUpDialog(eventId); // if th3e user is not in the Attendees
                        }
                    }

                    @Override
                    public void onCheckFailed(Exception e) {
                        Log.e(TAG, "User in Event attendees failed");
                    }
                });
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}