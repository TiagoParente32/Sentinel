package pt.ipleiria.estg.dei.sentinel;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText inputEmailField;
    private EditText inputPasswordField;
    private Button btnLogin;
    private Button btnRegister;
    private CheckBox checboxSignedIn;


    private FirebaseAuth mAuth;
    private FirebaseApp firebase;

    private boolean keepSignedIn = false;

    private static final String TAG = "EmailPassword";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();

        inputEmailField = findViewById(R.id.inputEmail);
        inputPasswordField = findViewById(R.id.inputPassword);
        checboxSignedIn = findViewById(R.id.chbSignedIn);

        //binds methods to button
        findViewById(R.id.btnLogin).setOnClickListener(this);
        findViewById(R.id.btnRegister).setOnClickListener(this);

    }

    @Override
    protected void onStart() {

        super.onStart();


            FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null){
            //OPENS DASHBOARD ACTIVITY
            Toast.makeText(LoginActivity.this,"LOGADO",Toast.LENGTH_LONG).show();
        }


    }

    private void updateUI(FirebaseUser currentUser) {
        //AFTER USER LOG IN INITIALIZES DASHBOARD ACTIVITY WITH USER'S NAME
        if(currentUser == null){
            Toast.makeText(LoginActivity.this,"Wrong username/password!",Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(LoginActivity.this,"Welcome " + currentUser.getEmail(),Toast.LENGTH_LONG).show();


    }


    private void signIn(String email, String password){

        if(email.length() <= 0 ){
            inputEmailField.setError("Email field is mandatory!");
            return;
        }if(password.length() <= 0){
            inputPasswordField.setError("Password has a minimum of 8 characters!");
            return;
        }

        if(checboxSignedIn.isChecked())
            keepSignedIn = true;

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
        FirebaseAuth.getInstance().signOut();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if(i == R.id.btnLogin){
            signIn(inputEmailField.getText().toString(),inputPasswordField.getText().toString());
        }
        if(i == R.id.btnRegister){
            Toast.makeText(LoginActivity.this,"DESLOGADO",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (!keepSignedIn)
            signOut();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!keepSignedIn)
            signOut();
    }
}
