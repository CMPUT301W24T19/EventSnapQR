package com.example.eventsnapqr;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * display the QR code of a given event. gives the capability to share or save the QRcode
 * as an image.
 */
public class QRDialogFragment extends DialogFragment {
    private ImageView imageQR;
    private Button buttonExit;
    private Button buttonSaveQR;
    private Bitmap bitmap;

    /**
     * What should be executed when the fragment is created
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Setup actions to be taken upon view creation and when the views are interacted with
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return the final view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_q_r_dialog, container, false);

        // Retrieve data from the bundle
        Bundle bundle = getArguments();
        if (bundle != null) {
            bitmap = bundle.getParcelable("bitmap");
            if (bitmap != null) {
                // Setting the QR code bitmap to ImageView
                imageQR = view.findViewById(R.id.imageview_qr);
                imageQR.setImageBitmap(bitmap);

                // Log to ensure bitmap is received correctly
                Log.d("QRDialogFragment", "Bitmap received successfully");
            } else {
                Log.e("QRDialogFragment", "Bitmap is null");
            }
        } else {
            Log.e("QRDialogFragment", "Bundle is null");
        }

        buttonExit = view.findViewById(R.id.button_exit);
        buttonExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), MainActivity.class);
                startActivity(intent);
            }
        });
        buttonSaveQR = view.findViewById(R.id.button_save_qr);

        buttonSaveQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String filename = "QRCode.png";
                    FileOutputStream outputStream = null;
                    try {
                        outputStream = getContext().openFileOutput(filename, Context.MODE_PRIVATE);
                    } catch (FileNotFoundException ex) {
                        throw new RuntimeException(ex);
                    }
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                    outputStream.close();

                    MediaStore.Images.Media.insertImage(getContext().getContentResolver(),bitmap,"QR Code",null);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                shareImage(bitmap);
            }
        });
        return view;
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
        File folder = new File(getContext().getCacheDir(),"images");
        Uri uri = null;
        try{
            folder.mkdir();
            File file = new File(folder,"image.jpg");
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG,90,fileOutputStream);
            fileOutputStream.flush();;
            fileOutputStream.close();
            uri = FileProvider.getUriForFile(getContext(),"com.example.eventsnapqr",file);
        } catch (Exception e){
            e.printStackTrace();
            Toast.makeText(getContext()," "+e.getMessage(),Toast.LENGTH_SHORT).show();
        }
        return uri;
    }
}