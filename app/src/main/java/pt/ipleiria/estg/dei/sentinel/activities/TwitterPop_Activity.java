package pt.ipleiria.estg.dei.sentinel.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import pt.ipleiria.estg.dei.sentinel.Constants;
import pt.ipleiria.estg.dei.sentinel.R;

public class TwitterPop_Activity extends AppCompatActivity {

    private ImageButton btnClose;
    private Button btnTweet;
    private EditText inputTweet;
    private TextView viewCounter;

    private String temperature;
    private String humidity;
    private String location;
    private String tweetMessage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_twitter_pop);

        View v = findViewById(android.R.id.content);

        v.setClipToOutline(true);

        btnClose = findViewById(R.id.btnClose);
        btnTweet = findViewById(R.id.btnTweet);
        viewCounter= findViewById(R.id.counterView);
        inputTweet = findViewById(R.id.inputTweet);

        /*GETS HUMIDITY AND TEMPERATURE DATA TO SHARE*/
        temperature = getIntent().getStringExtra(Constants.DATA_INTENT_TEMPERATURE);
        humidity = getIntent().getStringExtra(Constants.DATA_INTENT_HUMIDITY);
        location = getIntent().getStringExtra(Constants.DATA_INTENT_LOCATION);

        /*CREATES CUSTOM TWEET MESSAGE*/
        tweetMessage = "It's currently " + temperature + " and the humidity is at " + humidity + " on "+ location +"! Tweeted using @SentinelIPL #sentinelapp";


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


        /*LOADS CUSTOM MESSAGE INTO TWEET BOX*/
        inputTweet.setText(tweetMessage);

        /*UPDATES COUNTER*/
        viewCounter.setText(tweetMessage.length() + "/280");



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

         inputTweet.addTextChangedListener(mTextEditorWatcher);



    }


    public void tweet(){

        MainActivity.tweet(inputTweet.getText().toString(),getApplicationContext());
        finish();

    }
}
