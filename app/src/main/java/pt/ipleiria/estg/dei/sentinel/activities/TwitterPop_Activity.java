package pt.ipleiria.estg.dei.sentinel.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import pt.ipleiria.estg.dei.sentinel.R;

public class TwitterPop_Activity extends AppCompatActivity {

    private ImageButton btnClose;
    private Button btnTweet;
    private EditText tweetText;
    private TextView viewCounter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_twitter_pop);


        btnClose = findViewById(R.id.btnClose);
        btnTweet = findViewById(R.id.btnTweet);
        viewCounter= findViewById(R.id.counterView);
        tweetText = findViewById(R.id.inputTweet);

        /*CLOSES TWEET LAYOUT*/
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tweet();
            }
        });

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);


        int width = dm.widthPixels;
        int height = dm.heightPixels;


        getWindow().setLayout((int)(width*.9),(int)(height*.4));


        /*LIVE CHARACTER COUNT*/
         TextWatcher mTextEditorWatcher = new TextWatcher() {
            String textToDisplay = null;

             @Override
             public void beforeTextChanged(CharSequence s, int start, int count, int after) {

             }

             public void onTextChanged(CharSequence s, int start, int before, int count) {
                //This sets a textview to the current length
                textToDisplay = s.length() + "/280";
                viewCounter.setText(textToDisplay);
            }

             @Override
             public void afterTextChanged(Editable s) {

             }

         };

         tweetText.addTextChangedListener(mTextEditorWatcher);



    }


    public void tweet(){

        MainActivity.tweet(tweetText.getText().toString(),getApplicationContext());
        finish();

    }
}
