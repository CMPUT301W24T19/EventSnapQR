package com.example.eventsnapqr;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link QRDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */

public class QRDialogFragment extends DialogFragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private ImageView imageQR;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Button buttonExit;
    private Button buttonSaveQR;
    public QRDialogFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment QRDialogFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static QRDialogFragment newInstance(String param1, String param2) {
        QRDialogFragment fragment = new QRDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }
    private Bitmap bitmap;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_q_r_dialog, container, false);
//        Bundle bundle = getArguments();
//        if (bundle != null) {
//            bitmap = bundle.getParcelable("bitmap");
//            if (bitmap != null) {
//                imageQR = view.findViewById(R.id.imageview_qr);
//                imageQR.setImageBitmap(bitmap);
//            }
//        }
        String textToEncode = "Event Barcode";

        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
        try{
            bitmap = barcodeEncoder.encodeBitmap(textToEncode, BarcodeFormat.QR_CODE, 400, 400);
        } catch (WriterException e) {
            e.printStackTrace();
        }

        if (bitmap != null) {
            // Setting the QR code bitmap to ImageView

            imageQR = view.findViewById(R.id.imageview_qr);
            imageQR.setImageBitmap(bitmap);
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
                    Toast.makeText(getContext(), "QR Code saved successfully", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Failed to save QR Code", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }
}