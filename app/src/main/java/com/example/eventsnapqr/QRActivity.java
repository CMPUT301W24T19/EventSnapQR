package com.example.eventsnapqr;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * display the QR code of a given event. gives the capability to share or save the QRcode
 * as an image.
 */
public class QRActivity extends AppCompatActivity {
    private ImageView imageQR;
    private ImageView buttonExit;
    private MaterialButton buttonSaveQR;
    private Bitmap qrBitmap;
    private String QR;

    /**
     * What should be executed when the fragment is created
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);
        // Retrieve data from the bundle
        Bundle bundle = getIntent().getExtras();
        QR = bundle.getString("QR");
        Log.d("EVENT ID QR DIALOG: ", QR);
        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        try {
            qrBitmap = barcodeEncoder.encodeBitmap(QR, BarcodeFormat.QR_CODE, 400, 400);
            if (qrBitmap != null) {
                Log.d("QR_CODE", "QR Code generated successfully");
            } else {
                Log.e("QR_CODE", "Failed to generate QR Code: Bitmap is null");
            }
        } catch (WriterException e) {
            e.printStackTrace();
            Log.e("QR_CODE", "Failed to generate QR Code: " + e.getMessage());
        }

        if (qrBitmap != null) {
            imageQR = findViewById(R.id.imageview_qr);
            imageQR.setImageBitmap(qrBitmap);
            Log.d("QRActivity", "Bitmap received successfully");
        } else {
            Log.e("QRActivity", "Bitmap is null");
        }

        buttonExit = findViewById(R.id.button_back);
        buttonExit.setOnClickListener(new View.OnClickListener() {
            String destination = bundle.getString("destination");
            @Override
            public void onClick(View v) {
                if(destination != null){
                    if (destination.equals("manage")) {
                        getOnBackPressedDispatcher().onBackPressed();
                    } else if (destination.equals("main")) {
                        finish();
                    }
                }
            }
        });

        findViewById(R.id.button_save_qr).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String filename = "QRCode.png";
                    FileOutputStream outputStream = null;
                    try {
                        outputStream = getBaseContext().openFileOutput(filename, Context.MODE_PRIVATE);
                    } catch (FileNotFoundException ex) {
                        throw new RuntimeException(ex);
                    }
                    qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                    outputStream.close();

                    MediaStore.Images.Media.insertImage(getBaseContext().getContentResolver(),qrBitmap,"QR Code",null);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                shareImage(qrBitmap);
            }
        });
    }

    /**
     * implements and displays the ability to share the QR code
     * @param bitmap
     */
    private void shareImage(Bitmap bitmap){
        Uri uri = getImageToShare(bitmap);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM,uri);
        intent.putExtra(Intent.EXTRA_TEXT,"Sharing event QR code");
        intent.putExtra(Intent.EXTRA_SUBJECT,"Attached QR code Image");
        intent.setType("image/*");
        startActivity(Intent.createChooser(intent,"Share via"));
    }

    /**
     * fetch the URI of the QR code to share
     * @param bitmap bitmap that represents the QR code
     * @return URI of the resulting image
     */
    private Uri getImageToShare(Bitmap bitmap){
        File folder = new File(getBaseContext().getCacheDir(),"images");
        Uri uri = null;
        try{
            folder.mkdir();
            File file = new File(folder,"image.jpg");
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG,90,fileOutputStream);
            fileOutputStream.flush();;
            fileOutputStream.close();
            uri = FileProvider.getUriForFile(getBaseContext(),"com.example.eventsnapqr",file);
        } catch (Exception e){
            e.printStackTrace();
            Toast.makeText(getBaseContext()," "+e.getMessage(),Toast.LENGTH_SHORT).show();
        }
        return uri;
    }
}