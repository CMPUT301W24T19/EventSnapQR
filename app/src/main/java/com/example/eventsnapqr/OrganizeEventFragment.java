package com.example.eventsnapqr;

import static android.content.Context.WINDOW_SERVICE;
import static androidx.core.content.ContextCompat.getSystemService;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link OrganizeEventFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OrganizeEventFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private ImageButton buttonBackButton;
    private Button buttonAddEvent;

    private Bitmap bitmap;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public OrganizeEventFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment OrganizeEventFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static OrganizeEventFragment newInstance(String param1, String param2) {
        OrganizeEventFragment fragment = new OrganizeEventFragment();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_organize_event, container, false);
        buttonBackButton = view.findViewById(R.id.button_back_button);

        buttonBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                navController.navigate(R.id.action_organizeEventFragment_to_mainPageFragment);
            }
        });
        buttonAddEvent = view.findViewById(R.id.button_add_event);

        buttonAddEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QRGEncoder qrgEncoder = new QRGEncoder("Link goes here", null, QRGContents.Type.TEXT, 5);
                qrgEncoder.setColorBlack(Color.RED);
                qrgEncoder.setColorWhite(Color.BLUE);
                try {
                    bitmap = qrgEncoder.getBitmap();
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("bitmap", bitmap);
                    NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                    navController.navigate(R.id.action_organizeEventFragment_to_qRDialogFragment, bundle);
                } catch (Exception e) {
                    Log.v("Could not save qr code", e.toString());
                }


        }
        });
        return view;
    }
}