package pt.ipleiria.estg.dei.sentinel;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private TextInputLayout inputEmail;
    private TextInputLayout inputPassword;
    private CheckBox checkboxSignedIn;


    private FirebaseAuth mAuth;

    private boolean keepSignedIn;

    private static final String TAG = "EmailPassword";
    private static final String PREFERENCES_FILE_NAME = "pt.ipleiria.estg.dei.sentinel.SHARED_PREFERENCES";

    private SharedPreferences sharedPref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();


        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        checkboxSignedIn = findViewById(R.id.chbSignedIn);

        //binds methods to button
        findViewById(R.id.btnLogin).setOnClickListener(this);
        findViewById(R.id.btnRegister).setOnClickListener(this);

         sharedPref = this.getSharedPreferences(PREFERENCES_FILE_NAME,Context.MODE_PRIVATE);


    }

    @Override
    protected void onStart() {

        super.onStart();

        getSupportActionBar().setTitle("LOGIN");

        /*CHECKS USER SAVED PREFERENCE TO KEEP SIGNED IN */
        keepSignedIn = sharedPref.getBoolean("keep_signed_in",false);

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null){
            //OPENS DASHBOARD ACTIVITY
            Intent intent = new Intent(LoginActivity.this,DashboardActivity.class);
            startActivity(intent);
            finish();

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
        Intent intent = new Intent(LoginActivity.this,DashboardActivity.class);
        startActivity(intent);
        finish();

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
        sharedPref.edit().putBoolean("keep_signed_in",keepSignedIn).commit();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
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

    private void signOut(){
        sharedPref.edit().putBoolean("keep_signed_in",false).commit();
        FirebaseAuth.getInstance().signOut();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if(i == R.id.btnLogin){
            signIn(inputEmail.getEditText().getText().toString().trim(), inputPassword.getEditText().getText().toString().trim());
        }
        if(i == R.id.btnRegister){
            /*OPENS REGISTER ACTIVITY*/
            Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
            startActivity(intent);
            finish();
        }
    }

    /* IF USER PREFERENCE KEEP SIGNED IN IS FALSE, SIGNOUT USER BEFORE APP CLOSURE*/
    @Override
    public void onStop() {
        super.onStop();
        if (!keepSignedIn)
            signOut();
    }

}
