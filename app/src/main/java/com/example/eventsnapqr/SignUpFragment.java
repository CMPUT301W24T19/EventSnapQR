package com.example.eventsnapqr;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SignUpFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SignUpFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private Button buttonSignUp;
    private EditText editTextName;
    private EditText editTextEmail;
    private EditText editTextNumber;
    private EditText editTextHomepage;
    private FirebaseController firebaseController;

    public SignUpFragment() {
        // Required empty public constructor
    }

    public static SignUpFragment newInstance(String param1, String param2) {
        SignUpFragment fragment = new SignUpFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    private String androidId;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            androidId = getArguments().getString("userId");
        }
        firebaseController = new FirebaseController();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_sign_up, container, false);
        editTextEmail = v.findViewById(R.id.edit_text_email);

        editTextHomepage = v.findViewById(R.id.edit_text_homepage);
        String homepageInput = editTextHomepage.getText().toString().trim().isEmpty() ? null : editTextHomepage.getText().toString().trim();

        editTextName = v.findViewById(R.id.edit_text_name);

        // ensure correct number formatting
        editTextNumber = v.findViewById(R.id.edit_text_number);
        editTextNumber.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        editTextNumber.setFilters(new InputFilter[]{new InputFilter.LengthFilter(14)});

        buttonSignUp = v.findViewById(R.id.button_sign_up);
        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validateInput()){
                    User newUser = new User(editTextName.getText().toString(), androidId,
                            editTextHomepage.getText().toString(), editTextNumber.getText().toString(),
                            editTextEmail.getText().toString());
                    firebaseController.addUser(newUser);
                    goToMainPage();
                }

            }
        });
        return v;
    }
    public void goToMainPage(){
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        navController.navigate(R.id.mainPageFragment);
    }
    public Boolean validateInput(){
        if(editTextName.getText().toString().trim().isEmpty()){
            editTextName.setError("Name field cannot be empty");
            return false;
        }

        String emailInput = editTextEmail.getText().toString().trim();
        if(emailInput.isEmpty()){
            editTextEmail.setError("Email field cannot be empty");
            return false;
        } else if(!android.util.Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()){
            editTextEmail.setError("Please enter a valid email address");
            return false;
        }

        if(editTextNumber.getText().toString().trim().isEmpty()){
            editTextNumber.setError("Number field cannot be empty");
            return false;
        }
        return true;
    }

}