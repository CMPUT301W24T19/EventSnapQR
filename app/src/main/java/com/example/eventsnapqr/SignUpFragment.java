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

/**
 * fragment in which the user can enter their details for the event
 */
public class SignUpFragment extends Fragment {
    private Button buttonSignUp;
    private EditText editTextName;
    private EditText editTextEmail;
    private EditText editTextNumber;
    private EditText editTextHomepage;
    private FirebaseController firebaseController;
    private String androidId;

    /**
     * What should be executed when the fragment is created
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            androidId = getArguments().getString("userId");
        }
        firebaseController = new FirebaseController();
    }

    /**
     * Setup actions to be taken upon view creation and when the views are interacted with.
     * Validate the phone number and the email address, ensure no fields are blank apart from
     * homepage
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

    /**
     * direct to the main page upon sign up completion
     */
    public void goToMainPage(){
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        navController.navigate(R.id.mainPageFragment);
    }

    /**
     * validate the inputs of each of the fields
     * @return input validity
     */
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