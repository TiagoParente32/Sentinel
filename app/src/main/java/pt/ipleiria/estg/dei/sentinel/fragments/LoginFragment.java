package pt.ipleiria.estg.dei.sentinel.fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pt.ipleiria.estg.dei.sentinel.R;

public class LoginFragment extends Fragment implements View.OnClickListener{

    private TextInputLayout inputEmail;
    private TextInputLayout inputPassword;
    private CheckBox checkboxSignedIn;
    private boolean keepSignedIn;

    private FirebaseAuth mAuth;


    private static final String TAG = "EmailPassword";
    protected static final String PREFERENCES_FILE_NAME = "pt.ipleiria.estg.dei.sentinel.SHARED_PREFERENCES";


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_login,container,false);

        inputEmail = view.findViewById(R.id.inputEmail);
        inputPassword = view.findViewById(R.id.inputPassword);
        checkboxSignedIn = view.findViewById(R.id.chbSignedIn);

        //binds methods to button
        view.findViewById(R.id.btnLogin).setOnClickListener(this);
        view.findViewById(R.id.btnSave).setOnClickListener(this);

        return view;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().setTitle("LOGIN");


        mAuth = FirebaseAuth.getInstance();

    }

    @Override
    public void onStart() {

        super.onStart();

        //getActivity().getSupportActionBar().setTitle("LOGIN");


        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null){
            //OPENS DASHBOARD ACTIVITY
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new DashboardFragment()).commit();

        }


    }

    private void updateUI(FirebaseUser currentUser) {
        //AFTER USER LOG IN INITIALIZES DASHBOARD ACTIVITY WITH USER'S NAME
        //TEMPORARY
        if(currentUser == null){
            inputEmail.setError("Invalid email/password combination");
            inputPassword.getEditText().getText().clear();
            inputEmail.requestFocus();
            return;
        }

        /*OPENS DASHBOARD ACITIVITY*/
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new DashboardFragment()).commit();

    }

    public static boolean isEmailValid(String email) {

        /*MINIUM 8 CHARACTERS, 1 NUMBER AND 1 SPECIAL CHARACTER*/
        String regExpn = "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$";

        CharSequence inputStr = email;
        Pattern pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches())
            return true;
        else
            return false;
    }



    private void signIn(String email, String password){

        /*CLEANS POSSIBLE ERROR MESSAGES*/
        inputPassword.setError(null);
        inputEmail.setError(null);

        if(email.isEmpty()){
            inputEmail.setError("Email field is mandatory");
            inputEmail.requestFocus();
            return;
        }

        if(!isEmailValid(email)) {
            inputEmail.setError("Email format must be example@email.com");
            inputEmail.requestFocus();
            return;
        }if(password.isEmpty()){
            inputPassword.setError("Password field is mandatory");
            inputPassword.requestFocus();
            return;
        }

        /*SAVES USER PREFERENCE TO KEEP SIGNED IN AFTER APP CLOSE OR NOT IN SHARED PREFERENCES FILE*/
        keepSignedIn = checkboxSignedIn.isChecked();
        getActivity().getSharedPreferences(PREFERENCES_FILE_NAME,Context.MODE_PRIVATE).edit().putBoolean("keep_signed_in",keepSignedIn).commit();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            updateUI(null);
                        }

                        // ...
                    }
                });

        //STARTS DASHBOARD ACTIVITY


    }


    @Override
    public void onClick(View v) {
        int i = v.getId();
        if(i == R.id.btnLogin){

            signIn(inputEmail.getEditText().getText().toString().trim(), inputPassword.getEditText().getText().toString().trim());
        }
        if(i == R.id.btnSave){
            /*OPENS REGISTER ACTIVITY*/
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new RegisterFragment()).commit();
        }
    }



}
