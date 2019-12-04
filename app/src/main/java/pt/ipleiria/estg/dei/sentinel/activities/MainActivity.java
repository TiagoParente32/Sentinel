package pt.ipleiria.estg.dei.sentinel.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import pt.ipleiria.estg.dei.sentinel.Constants;
import pt.ipleiria.estg.dei.sentinel.R;
import pt.ipleiria.estg.dei.sentinel.fragments.DashboardFragment;
import pt.ipleiria.estg.dei.sentinel.fragments.LoginFragment;
import pt.ipleiria.estg.dei.sentinel.fragments.RegisterFragment;
import pt.ipleiria.estg.dei.sentinel.fragments.SendFragment;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int MBVIEW_REQUEST_CODE = 1;
    private DrawerLayout drawer;
    private TextView tvHeaderEmail;
    private SharedPreferences sharedPref;
    private NavigationView navigationView;
    private Configuration configuration;



    protected static final String PREFERENCES_FILE_NAME = "pt.ipleiria.estg.dei.sentinel.SHARED_PREFERENCES";


    /*TWITTER API*/

    private static Twitter twitter;
    private static RequestToken requestToken;
    private AccessToken accessToken;


    /*DATA TO PASS TO TWITTER ACITIVITY*/
    private String temperature;
    private String humidity;
    private String location;
    private String airQuality;

    //DATA TO PASS TO SEND FRAG
    private ArrayList<String> roomsList ;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sharedPref = getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);

        /*GETS URL FROM INTENT*/
        Uri uri = getIntent().getData();

            /*IF USER STARTED THE APP, NO URL EXISTS*/
            if (uri != null && uri.toString().startsWith(Constants.CALLBACKURL)) {

                /*GETS VERIFIER FROM URL*/
                final String verifier = uri.getQueryParameter(Constants.URL_TWITTER_OAUTH_VERIFIER);

                try {
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                accessToken = twitter.getOAuthAccessToken(
                                        requestToken, verifier);

                                SharedPreferences.Editor e = sharedPref.edit();


                                // After getting access token, access token secret
                                // store them in application preferences
                                e.putString(Constants.PREF_KEY_OAUTH_TOKEN, accessToken.getToken());
                                e.putString(Constants.PREF_KEY_OAUTH_SECRET, accessToken.getTokenSecret());
                                e.putBoolean(Constants.PREF_KEY_TWITTER_LOGIN, true);
                                e.putBoolean(Constants.KEEP_SIGNEDIN_TWITTER,false);
                                e.commit(); // save changes

                                Intent intent = new Intent(MainActivity.this,TwitterPop_Activity.class);
                                intent.putExtra(Constants.DATA_INTENT_TEMPERATURE, temperature);
                                intent.putExtra(Constants.DATA_INTENT_HUMIDITY, humidity);
                                intent.putExtra(Constants.DATA_INTENT_LOCATION, location);
                                intent.putExtra(Constants.DATA_INTENT_AIRQUALITY,airQuality);

                                startActivity(intent);


                                Log.v("accessToken", accessToken.getToken());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                    });
                    thread.start();


                } catch (Exception e) {
                    Log.v("Sentinel", "Erro => " + e.getMessage());
                }
            }




        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        tvHeaderEmail = navigationView.getHeaderView(0).findViewById(R.id.nav_email);


        /*THE STRINGS IN THE CONSTRUCTOR ARE HELPFUL FOR BLIND PEOPLE WHO NEED TEXT TO SPEECH*/
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close){
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                if (currentUser == null) {
                    tvHeaderEmail.setText(R.string.unauthenticated);

                    /*DISPLAYS LOGIN AND REGISTER BUTTONS*/
                    navigationView.getMenu().findItem(R.id.nav_logout).setVisible(false);
                    navigationView.getMenu().findItem(R.id.nav_login).setVisible(true);
                    navigationView.getMenu().findItem(R.id.nav_register).setVisible(true);
                    navigationView.getMenu().findItem(R.id.nav_send).setVisible(false);

                } else {
                    /*DISPLAYS LOGIN AND REGISTER BUTTONS*/
                    navigationView.getMenu().findItem(R.id.nav_logout).setVisible(true);
                    navigationView.getMenu().findItem(R.id.nav_login).setVisible(false);
                    navigationView.getMenu().findItem(R.id.nav_register).setVisible(false);
                    navigationView.getMenu().findItem(R.id.nav_send).setVisible(true);
                    tvHeaderEmail.setText(currentUser.getEmail());

                }
            }
        };

        drawer.addDrawerListener(toggle);
        toggle.syncState();


        /*SWITCHS TO DASHBOARD FRAGMENT*/
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new DashboardFragment()).commit();


    }


    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_send:
                SendFragment sendFragment = new SendFragment();
                Bundle bundle = new Bundle();
                bundle.putStringArrayList(Constants.DATA_INTENT_SPINNER_DATA,roomsList);
                sendFragment.setArguments(bundle);

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        sendFragment).commit();
                break;
            case R.id.nav_dashboard:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new DashboardFragment()).commit();
                break;

            case R.id.nav_login:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new LoginFragment()).commit();
                break;

            case R.id.nav_register:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new RegisterFragment()).commit();
                break;

            case R.id.nav_logout:
                signOut();


                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new DashboardFragment()).commit();

        }
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    private void signOut() {
        sharedPref.edit().putBoolean(Constants.KEEP_SIGNEDIN, false).commit();
        FirebaseAuth.getInstance().signOut();
    }


    private boolean isTwitterLoggedInAlready() {
        if(sharedPref.contains(Constants.PREF_KEY_TWITTER_LOGIN)){
            return sharedPref.getBoolean(Constants.PREF_KEY_TWITTER_LOGIN, false);
        }
        return false;
    }

    public void loginToTwitter() {
        // Check if already logged in
        if(!isTwitterLoggedInAlready()) {


            // Start new thread for activity (you can't do too much work on the UI/Main thread.
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {


                    // Setup builder
                    ConfigurationBuilder builder = new ConfigurationBuilder();
                    // Get key and secret from Constants.java
                    builder.setOAuthConsumerKey(Constants.API_KEY);
                    builder.setOAuthConsumerSecret(Constants.API_SECRET);

                    // Build
                    configuration = builder.build();

                    TwitterFactory factory = new TwitterFactory(configuration);
                    twitter = factory.getInstance();

                    try {
                        requestToken = twitter
                                .getOAuthRequestToken(Constants.CALLBACKURL);

                        sharedPref.edit().putBoolean(Constants.KEEP_SIGNEDIN_TWITTER,true).commit();

                        MainActivity.this.startActivity(new Intent(Intent.ACTION_VIEW, Uri
                                .parse(requestToken.getAuthenticationURL())));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();

        }else{
            Intent intent = new Intent(MainActivity.this,TwitterPop_Activity.class);
            intent.putExtra(Constants.DATA_INTENT_TEMPERATURE, temperature);
            intent.putExtra(Constants.DATA_INTENT_HUMIDITY, humidity);
            intent.putExtra(Constants.DATA_INTENT_LOCATION, location);
            intent.putExtra(Constants.DATA_INTENT_AIRQUALITY,airQuality);


            startActivity(intent);        }

    }

    public static void tweet(String tweet,Context context){

        Handler message = new Handler() {
            public void handleMessage(Message msg){
                if(msg.what == 0){
                    Toast.makeText(context, "Tweet sent successfully!", Toast.LENGTH_SHORT).show();
                }
                if(msg.what == 1){
                    Toast.makeText(context, "An error occurred while trying to send the Tweet!", Toast.LENGTH_SHORT).show();
                }
            }
        };



        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {

                try {
                    twitter.updateStatus(tweet);
                    message.sendEmptyMessage(0);

                } catch (Exception e) {
                    e.printStackTrace();
                    message.sendEmptyMessage(1);
                }
            }
        });
        thread.start();


    }

    public void setData(String temperature,String humidity, String location,String airQuality) {
        this.temperature = temperature;
        this.humidity = humidity;
        this.location = location;
        this.airQuality = airQuality;
    }

    public void setSpinnerData(List<String> roomsList) {
        this.roomsList = new ArrayList<>(roomsList);
    }


    /* IF USER PREFERENCE KEEP SIGNED IN IS FALSE, SIGNOUT USER BEFORE APP CLOSURE*/
    @Override
    public void onStop() {
        sharedPref.edit().putBoolean(Constants.PREF_KEY_TWITTER_LOGIN, false).commit();
        if (!sharedPref.getBoolean(Constants.KEEP_SIGNEDIN, false) && !sharedPref.getBoolean(Constants.KEEP_SIGNEDIN_TWITTER,false)){
            signOut();
        }
        super.onStop();
    }



}

