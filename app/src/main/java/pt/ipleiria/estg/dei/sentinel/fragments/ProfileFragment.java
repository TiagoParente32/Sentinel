package pt.ipleiria.estg.dei.sentinel.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pt.ipleiria.estg.dei.sentinel.Constants;
import pt.ipleiria.estg.dei.sentinel.CustomAdapter;
import pt.ipleiria.estg.dei.sentinel.R;

public class ProfileFragment extends Fragment {
    private TextView txtUserEmail;
    private TextView txtError;
    private TextInputLayout inputEmail;
    private TextInputLayout inputOldPassword;
    private TextInputLayout inputNewPassword;
    private TextInputLayout inputNewPasswordConf;
    private String oldEmail;
    private FirebaseUser user;
    private static final String TAG="REAUTHENTICATE_USER";
    private boolean signedIn;
    private SharedPreferences sharedPref;
    private CheckBox twitterSignIn;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        user = FirebaseAuth.getInstance().getCurrentUser();

        sharedPref = getActivity().getSharedPreferences(Constants.PREFERENCES_FILE_NAME,Context.MODE_PRIVATE);

        /*CHECKS IS TWITTER USER IS LOGGED IN*/
        signedIn = sharedPref.getBoolean(Constants.PREF_KEY_TWITTER_LOGIN,true);


        txtUserEmail = view.findViewById(R.id.txtUserEmail);
        inputEmail = view.findViewById(R.id.inputNewEmail);
        inputOldPassword = view.findViewById(R.id.inputOldPassword);
        inputNewPassword = view.findViewById(R.id.inputNewPassword);
        inputNewPasswordConf = view.findViewById(R.id.inputNewPasswordConf);
        txtError = view.findViewById(R.id.txtError);
        twitterSignIn = view.findViewById(R.id.clearTwitter);

        oldEmail = user.getEmail();

        txtUserEmail.setText(oldEmail);
        inputEmail.getEditText().setText(oldEmail);

        /*HIDES CHECKBOX ACCORDING TO TWITTER LOGIN STATE*/
        if(signedIn){
            twitterSignIn.setVisibility(View.VISIBLE);
        }else{
            twitterSignIn.setVisibility(View.INVISIBLE);
        }

        /*SETS ON BUTTON SAVE CLICK LISTENER*/
        view.findViewById(R.id.btnSave).setOnClickListener((v -> {
            checkInput();
        }));



        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().setTitle("PROFILE");



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

    private void checkInput(){
        String newEmail = inputEmail.getEditText().getText().toString().trim();
        String oldPassword = inputOldPassword.getEditText().getText().toString().trim();
        String newPassword = inputNewPassword.getEditText().getText().toString().trim();
        String newPasswordConf = inputNewPasswordConf.getEditText().getText().toString().trim();

        txtError.setText("");
        inputEmail.setError(null);
        inputOldPassword.setError(null);
        inputNewPasswordConf.setError(null);
        inputNewPassword.setError(null);

        if(oldPassword.isEmpty()){
            inputOldPassword.setError("Current Password field is mandatory to update Profile");
            inputOldPassword.requestFocus();
            return;
        }



        if(newEmail.isEmpty() && newPassword.isEmpty()){
            txtError.setText("Email or password must be filled in order to update");
            return;
        }

        if(newEmail.equals(user.getEmail()) && newPassword.isEmpty() && !twitterSignIn.isChecked()){
            txtError.setText("No changes made");
            clearInputs();
            return;
        }

        if(!newEmail.isEmpty() && !isEmailValid(newEmail)) {
            inputEmail.setError("Email format must be example@email.com");
            inputEmail.requestFocus();
            return;
        }

        if(!newPassword.isEmpty() && !isPasswordValid(newPassword)) {
            inputNewPassword.setError("Password must have at least have 8 digits, 1 number and 1 special character");
            inputNewPassword.requestFocus();
            clearInputs();
            return;
        }


        if(!newPasswordConf.equals(newPassword)){
            inputNewPasswordConf.setError("Password Confirmation doesn't match password");
            inputNewPasswordConf.requestFocus();
            clearInputs();
            return;
        }

        /*EVERYTHING GOES WELL*/
        reauthenticateUser(oldPassword,newPassword,newEmail);

    }

    private void reauthenticateUser(String oldPassword, String newPassword, String newEmail){

        AuthCredential credential = EmailAuthProvider.getCredential(oldEmail,oldPassword);

        /*REAUTHENTICATES USER*/
        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG, "User re-authenticated.");
                        if(task.isSuccessful()){
                            updateUser(newEmail,newPassword);
                            txtError.setText("Profile updated successfully");
                        } else {
                            txtError.setText("Wrong password. Error updating data");
                            clearInputs();
                        }
                    }
                });


    }

    private void updateUser(String email, String password){

        if(!email.isEmpty()){
            user.updateEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Email updated successfully");
                                inputEmail.getEditText().setText(user.getEmail());
                                txtUserEmail.setText(user.getEmail());
                                clearInputs();
                                txtError.setText("Profile updated successfully");
                            }
                            else {
                                txtError.setText("Error updating email");
                                clearInputs();
                            }

                        }
                    });
        }

        if(!password.isEmpty()){
            user.updatePassword(password)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Password updated successfully");
                                clearInputs();
                                txtError.setText("Profile updated successfully");
                            }
                            else {
                                txtError.setText("Error updating password");
                                clearInputs();
                            }

                        }
                    });
        }

        /*CLEAR TWITTER LOG IN*/
        if(twitterSignIn.isChecked()){
            try{

                SharedPreferences.Editor e = sharedPref.edit();

                e.putBoolean(Constants.PREF_KEY_TWITTER_LOGIN,false);
                e.putString(Constants.PREF_KEY_OAUTH_SECRET,"");
                e.putString(Constants.PREF_KEY_OAUTH_TOKEN,"");
                e.commit();

                Log.v("CLEAR_TWITTER_LOGIN","SUCCESSFULL");

            }catch (Exception ex){
                Log.v("CLEAR_TWITTER_LOGIN",ex.getMessage());
            }
        }




    }

    private void clearInputs(){
        inputOldPassword.getEditText().setText("");
        inputNewPassword.getEditText().setText("");
        inputNewPasswordConf.getEditText().setText("");
    }



}
