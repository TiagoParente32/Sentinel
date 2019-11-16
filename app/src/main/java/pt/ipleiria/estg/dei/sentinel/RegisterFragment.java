package pt.ipleiria.estg.dei.sentinel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterFragment extends Fragment implements View.OnClickListener{

    private TextInputLayout inputEmail;
    private TextInputLayout inputPassword;
    private TextInputLayout inputPasswordConf;

    private FirebaseAuth mAuth;

    private static final String TAG = "EmailPassword";



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().setTitle("REGISTER");


        mAuth = FirebaseAuth.getInstance();


    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_register,container,false);

        inputEmail = view.findViewById(R.id.inputEmail);
        inputPassword = view.findViewById(R.id.inputPassword);
        inputPasswordConf = view.findViewById(R.id.inputPasswordConf);

        //binds methods to button
        view.findViewById(R.id.btnRegister).setOnClickListener(this);


        return view;
    }

    /*@Override
    protected void onStart() {

        super.onStart();


        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null){
            //OPENS DASHBOARD ACTIVITY
            Intent intent = new Intent(RegisterFragment.this, DashboardFragment.class);
            startActivity(intent);
            finish();

        }


    }*/

    private void registerUser(String email, String password, String passwordConf){

        /*CLEANS PREVIOUS ERROR MESSAGES*/
        inputEmail.setError(null);
        inputPassword.setError(null);
        inputPasswordConf.setError(null);


        if(email.isEmpty()){
            inputEmail.setError("Email field is mandatory");
            inputEmail.requestFocus();
            return;
        }
        if(!isEmailValid(email)) {
            inputEmail.setError("Email format must be example@email.com");
            inputEmail.requestFocus();
            return;
        }

        if(password.isEmpty()){
           inputPassword.setError("Password field is mandatory");
           inputPassword.requestFocus();
           return;
        }

         if(!isPasswordValid(password)) {
             inputPassword.setError("Password must have at least have 8 digits, 1 number and 1 special character");
             inputPassword.requestFocus();
             return;
         }


        if(passwordConf.isEmpty()){
            inputPasswordConf.setError("Password confirmation field is mandatory");
            inputPasswordConf.requestFocus();
            return;
        }


        if(!passwordConf.equals(password)){
            inputPasswordConf.setError("Password Confirmation doesn't match password");
            inputPasswordConf.requestFocus();
            return;
        }

        /*USES FIREBASE TO CREATE THE USER*/
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            /*OPENS DASHBOARD ACITIVITY*/
                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                    new DashboardFragment()).commit();


                        } else {
                            try {
                                throw task.getException();
                            } catch(FirebaseAuthInvalidCredentialsException e) {
                                inputEmail.setError(getString(R.string.error_invalid_email));
                                inputEmail.requestFocus();
                            } catch(FirebaseAuthUserCollisionException e) {
                                inputEmail.setError(getString(R.string.error_user_exists));
                                inputEmail.requestFocus();
                            } catch(Exception e) {
                                Log.e(TAG, e.getMessage());
                            }
                        }

                    }
                });


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



    public static boolean isPasswordValid(String passsword) {

        String regExpn = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$";


        CharSequence inputStr = passsword;
        Pattern pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches())
            return true;
        else
            return false;
    }

    @Override
    public void onClick(View v){
        int i = v.getId();
        if(i == R.id.btnRegister){
            registerUser(inputEmail.getEditText().getText().toString().trim(),inputPassword.getEditText().getText().toString().trim(),inputPasswordConf.getEditText().getText().toString().trim());
        }

    }


}
