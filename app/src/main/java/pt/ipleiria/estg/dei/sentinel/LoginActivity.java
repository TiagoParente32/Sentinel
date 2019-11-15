package pt.ipleiria.estg.dei.sentinel;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private TextInputLayout inputEmailField;
    private TextInputLayout inputPasswordField;
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

        inputEmailField = findViewById(R.id.inputEmail);
        inputPasswordField = findViewById(R.id.inputPassword);
        checkboxSignedIn = findViewById(R.id.chbSignedIn);

        //binds methods to button
        findViewById(R.id.btnLogin).setOnClickListener(this);
        findViewById(R.id.btnRegister).setOnClickListener(this);

         sharedPref = this.getSharedPreferences(PREFERENCES_FILE_NAME,Context.MODE_PRIVATE);


    }

    @Override
    protected void onStart() {

        super.onStart();

        /*CHECKS USER SAVED PREFERENCE TO KEEP SIGNED IN */
        keepSignedIn = sharedPref.getBoolean("keep_signed_in",false);

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null){
            //OPENS DASHBOARD ACTIVITY


        }


    }

    private void updateUI(FirebaseUser currentUser) {
        //AFTER USER LOG IN INITIALIZES DASHBOARD ACTIVITY WITH USER'S NAME
        //TEMPORARY
        if(currentUser == null){
            inputEmailField.setError("Invalid email/password combination");
            inputEmailField.requestFocus();
            return;
        }

        /*OPENS DASHBOARD ACITIVITY*/

    }


    private void signIn(String email, String password){

        /*CLEANS POSSIBLE ERROR MESSAGES*/
        inputPasswordField.setError(null);
        inputEmailField.setError(null);

        if(email.isEmpty() ){
            inputEmailField.setError("Email field is mandatory");
            inputEmailField.requestFocus();
            return;
        }if(password.isEmpty()){
            inputPasswordField.setError("Password field is mandatory");
            inputPasswordField.requestFocus();
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
            signIn(inputEmailField.getEditText().getText().toString().trim(),inputPasswordField.getEditText().getText().toString().trim());
        }
        if(i == R.id.btnRegister){
            /*OPENS REGISTER ACTIVITY*/
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
