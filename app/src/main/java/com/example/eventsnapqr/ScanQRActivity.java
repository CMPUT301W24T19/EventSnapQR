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

import com.google.firebase.Firebase;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import android.Manifest;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.util.concurrent.CompletableFuture;

/**
 * activity where the QR scanner is implemented using ZXing library
 * the following video was a major help and source for setting up
 * this functionality
 *
 * https://www.youtube.com/watch?v=bWEt-_z7BOY&ab_channel=EasyOneCoders
 * the view event details after scanning an event you have not signed up
 * for is not currently linked to the event page. planned for project part 4
 */
public class ScanQRActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CAMERA = 1;
    private String userId;

    /**
     * What should be executed when the fragment is created
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
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

    /**
     * integrate the qr scanner once permissions are verified
     */
    private void initQRCodeScanner() {
        // https://stackoverflow.com/questions/34983201/change-qr-scanner-orientation-with-zxing-in-android-studio
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setOrientationLocked(false);
        integrator.setPrompt("Scan QR Code");
        integrator.setCameraId(0);
        integrator.initiateScan();
    }

    /**
     * if the user has not signed up scanned event, give them the option to view the event details
     * or to return to the main page
     * @param eventId identifier of the given event
     */
    private void notSignedUpDialog(String eventId) {
        if (!isFinishing()) {
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
                            finish();
                        }
                    })
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            // finish activity if dialog is dismissed without button press
                            finish();
                        }
                    })
                    .create()
                    .show();
        }
    }


    /**
     * display an alert dialog notifying the user they have successfully checked into the event
     * and return to the main page
     * @param eventId the unique identifier of the given event
     */
    private void checkIn(String eventId) {
        FirebaseController.getInstance().incrementCheckIn(userId, eventId, new FirebaseController.CheckInListener() {
            @Override
            public void onCheckInComplete(int count) {
                FirebaseController.getInstance().getEvent(eventId, new FirebaseController.OnEventRetrievedListener() {
                    @Override
                    public void onEventRetrieved(Event event) {
                        if (event != null) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(ScanQRActivity.this);
                            builder.setTitle("You have checked into " + event.getEventName() + " for the " + count + getSuffix(count) + " time!")
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            finish();
                                        }
                                    })
                                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                        @Override
                                        public void onDismiss(DialogInterface dialog) {
                                            finish();
                                        }
                                    });
                            builder.create().show();
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(ScanQRActivity.this);
                            builder.setMessage("Failed to retrieve event details.")
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            finish();
                                        }
                                    })
                                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                        @Override
                                        public void onDismiss(DialogInterface dialog) {
                                            finish();
                                        }
                                    });
                            builder.create().show();
                        }
                    }
                });
            }

            @Override
            public void onCheckInFailure(Exception e) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ScanQRActivity.this);
                builder.setMessage("Failed to increment check-in count.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        })
                        .setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                // Finish activity if dialog is dismissed without user interaction
                                finish();
                            }
                        });
                builder.create().show();
            }
        });
    }

    /**
     * returns the correct suffix for the given integer
     * @param n the number in which to retrieve the suffix
     */
    public String getSuffix(Integer n) {
        if (n >= 11 && n <= 13) {
            return "th";
        } else {
            switch (n % 10) {
                case 1:
                    return "st";
                case 2:
                    return "nd";
                case 3:
                    return "rd";
                default:
                    return "th";
            }
        }
    }

    /**
     * handles what to do with the content of the QR code
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode The integer result code returned by the child activity
     *                   through its setResult().
     * @param data An Intent, which can return result data to the caller
     *               (various data can be attached to Intent "extras").
     *
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (intentResult != null) {
            String contents = intentResult.getContents();
            if (contents != null) {
                Log.d(TAG, "QR code content: " + contents); // Log the content of the QR code

                String eventId = contents.trim();

            if (eventId.length() != 20) {
                showInvalidQRContentDialog();
            }

                FirebaseController.getInstance().checkUserInAttendees(eventId, userId, new FirebaseController.OnUserInAttendeesListener() {
                    @Override
                    public void onUserInAttendees(boolean isInAttendees) {
                        if (isInAttendees) {
                            Log.d(TAG, "User is in attendees");
                            checkIn(eventId);
                        } else {
                            Log.d(TAG, "User is not in attendees");
                            notSignedUpDialog(eventId);
                        }
                    }

                    @Override
                    public void onCheckFailed(Exception e) {
                        Log.e(TAG, "User in Event attendees check failed: " + e.getMessage());
                        showInvalidQRContentDialog();
                    }
                });
            } else {
                // case when no QR code was scanned before camera closed
                Log.d(TAG, "No QR code scanned before camera closed");
                finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Displays a dialog to inform the user about the invalid QR code content.
     */
    private void showInvalidQRContentDialog() {
        if (!isFinishing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ScanQRActivity.this);
            builder.setTitle("Invalid QR Code Content")
                    .setMessage("The scanned QR code contains an invalid content.")
                    .setPositiveButton("Scan Another QR Code", (dialog, which) -> {
                        initQRCodeScanner();
                    })
                    .setNegativeButton("Return", (dialog, which) -> {
                        finish();
                    })
                    .setOnDismissListener(dialog -> {
                        finish();
                    })
                    .create()
                    .show();
        }
    }
}