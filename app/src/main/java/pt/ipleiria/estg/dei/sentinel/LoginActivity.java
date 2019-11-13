package pt.ipleiria.estg.dei.sentinel;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText inputEmailField;
    private EditText inputPasswordField;

    private Button btnLogin;
    private FirebaseAuth mAuth;

    private static final String TAG = "EmailPassword";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();

        inputEmailField = findViewById(R.id.inputEmail);
        inputPasswordField = findViewById(R.id.inputPassword);

        //binds methods to button
        findViewById(R.id.btnLogin).setOnClickListener(this);

    }

    @Override
    protected void onStart() {

        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();


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
    }
}
