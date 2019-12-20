package pt.ipleiria.estg.dei.sentinel.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.sip.SipSession;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;



import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;

import pt.ipleiria.estg.dei.sentinel.Constants;
import pt.ipleiria.estg.dei.sentinel.R;
import pt.ipleiria.estg.dei.sentinel.fragments.DashboardFragment;
import pt.ipleiria.estg.dei.sentinel.fragments.FavoritesFragment;
import pt.ipleiria.estg.dei.sentinel.fragments.LoginFragment;
import pt.ipleiria.estg.dei.sentinel.fragments.MyExposureFragment;
import pt.ipleiria.estg.dei.sentinel.fragments.NotificationsFragment;
import pt.ipleiria.estg.dei.sentinel.fragments.ProfileFragment;
import pt.ipleiria.estg.dei.sentinel.fragments.RegisterFragment;
import pt.ipleiria.estg.dei.sentinel.fragments.SendFragment;
import pt.ipleiria.estg.dei.sentinel.fragments.StatisticsFragment;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;
    private TextView tvHeaderEmail;
    public SharedPreferences sharedPref;
    private NavigationView navigationView;
    private Configuration configuration;
    private TextView tvNotificationCounter;
    private Toolbar toolbar;
    private FirebaseUser currentUser;



    /*TWITTER API*/

    private static Twitter twitter;
    private static RequestToken requestToken;
    private AccessToken accessToken;


    /*DATA TO PASS TO TWITTER ACITIVITY*/
    private String temperature;
    private String humidity;
    private String location;
    private String airQuality;
    private int notificationCounter;
    private String counter;

    //DATA TO PASS TO SEND FRAG
    private ArrayList<String> roomsList ;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sharedPref = getSharedPreferences(Constants.PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);


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
                                e.putBoolean(Constants.PREF_KEY_TWITTER_LOGIN,true).commit();

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
        tvNotificationCounter = findViewById(R.id.txtNotificationsNumber);
        this.toolbar = findViewById(R.id.toolbar);

        navigationView.setNavigationItemSelectedListener(this);
        findViewById(R.id.btnNotifications).setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new NotificationsFragment()).commit();
        });


        tvHeaderEmail = navigationView.getHeaderView(0).findViewById(R.id.nav_email);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();



        /*THE STRINGS IN THE CONSTRUCTOR ARE HELPFUL FOR BLIND PEOPLE WHO NEED TEXT TO SPEECH*/
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close){
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null) {
                    tvHeaderEmail.setText(R.string.unauthenticated);

                    /*DISPLAYS LOGIN AND REGISTER BUTTONS*/
                    navigationView.getMenu().findItem(R.id.nav_logout).setVisible(false);
                    navigationView.getMenu().findItem(R.id.nav_login).setVisible(true);
                    navigationView.getMenu().findItem(R.id.nav_register).setVisible(true);
                    navigationView.getMenu().findItem(R.id.nav_favorites).setVisible(false);
                    navigationView.getMenu().findItem(R.id.nav_send).setVisible(false);
                    navigationView.getMenu().findItem(R.id.nav_exposure).setVisible(false);
                    navigationView.getMenu().findItem(R.id.nav_statistics).setVisible(false);
                    navigationView.getMenu().findItem(R.id.nav_profile).setVisible(false);


                } else {
                    /*DISPLAYS LOGIN AND REGISTER BUTTONS*/
                    navigationView.getMenu().findItem(R.id.nav_logout).setVisible(true);
                    navigationView.getMenu().findItem(R.id.nav_login).setVisible(false);
                    navigationView.getMenu().findItem(R.id.nav_register).setVisible(false);
                    navigationView.getMenu().findItem(R.id.nav_favorites).setVisible(true);
                    navigationView.getMenu().findItem(R.id.nav_exposure).setVisible(true);
                    navigationView.getMenu().findItem(R.id.nav_profile).setVisible(true);
                    navigationView.getMenu().findItem(R.id.nav_send).setVisible(true);

                    navigationView.getMenu().findItem(R.id.nav_statistics).setVisible(true);
                    tvHeaderEmail.setText(user.getEmail());

                }
            }
        };

        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if(currentUser == null){
            hideToolbarItens();
        }else{
            readNotificationCounter();
        }


        /*SETS SHARED PREFERENCES LISTENER TO UPDATE NOTIFICATION COUNTER*/
        SharedPreferences.OnSharedPreferenceChangeListener listener = (prefs, key) -> {
            if(key.equals(Constants.PREFERENCES_NOTIFICATIONS_UNREAD) || key.equals(Constants.PREFERENCES_LOGGED_IN)){
                this.runOnUiThread(() -> readNotificationCounter());
            }
        };

        sharedPref.registerOnSharedPreferenceChangeListener(listener);


        /*SWITCHS TO DASHBOARD FRAGMENT*/
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new DashboardFragment()).commit();


    }

    private void readNotificationCounter(){
        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            showToolbarItens();
            try {
                notificationCounter = sharedPref.getInt(Constants.PREFERENCES_NOTIFICATIONS_UNREAD, 0);

                if (notificationCounter == 0) {
                    tvNotificationCounter.setVisibility(View.INVISIBLE);
                    return;
                }
                tvNotificationCounter.setVisibility(View.VISIBLE);
                counter = String.valueOf(notificationCounter);
                tvNotificationCounter.setText(counter);
            } catch (Exception ex) {
                Log.v("ERROR_READ_NOT_COUNTER", ex.getMessage());
            }
        }else{
            hideToolbarItens();
        }
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

    private void showToolbarItens(){
        toolbar.findViewById(R.id.txtNotificationsNumber).setVisibility(View.VISIBLE);
        toolbar.findViewById(R.id.btnNotifications).setVisibility(View.VISIBLE);
    }

    private void hideToolbarItens(){
        toolbar.findViewById(R.id.txtNotificationsNumber).setVisibility(View.INVISIBLE);
        toolbar.findViewById(R.id.btnNotifications).setVisibility(View.INVISIBLE);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_statistics:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new StatisticsFragment()).commit();
                break;
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

            case R.id.nav_favorites:
                FavoritesFragment favoritesFragment = new FavoritesFragment();

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        favoritesFragment).commit();
                break;

            case R.id.nav_exposure:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new MyExposureFragment()).commit();
                break;

            case R.id.nav_profile:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ProfileFragment()).commit();
                break;

            case R.id.nav_logout:
                signOut();
                hideToolbarItens();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new DashboardFragment()).commit();

        }
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    private void signOut() {
        hideToolbarItens();
        FirebaseAuth.getInstance().signOut();
        sharedPref.edit().putBoolean(Constants.PREFERENCES_LOGGED_IN,false).commit();
        sharedPref.edit().putBoolean(Constants.KEEP_SIGNEDIN, false).commit();
    }


    private boolean isTwitterLoggedInAlready() {
        if(sharedPref.contains(Constants.PREF_KEY_TWITTER_LOGIN)){
            return sharedPref.getBoolean(Constants.PREF_KEY_TWITTER_LOGIN, false);
        }
        return false;
    }

    public void loginToTwitter() {

            // Start new thread for activity/ cant do much on UI thread
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {


                    // Setup builder
                    ConfigurationBuilder builder = new ConfigurationBuilder();
                    // Get key and secret from Constants.java
                    builder.setOAuthConsumerKey(Constants.API_KEY);
                    builder.setOAuthConsumerSecret(Constants.API_SECRET);

                    // Check if already logged in
                    if(!isTwitterLoggedInAlready()) {

                        configuration = builder.build();
                        TwitterFactory factory = new TwitterFactory(configuration);
                        twitter = factory.getInstance();

                        try {
                            requestToken = twitter
                                    .getOAuthRequestToken(Constants.CALLBACKURL);


                            MainActivity.this.startActivity(new Intent(Intent.ACTION_VIEW, Uri
                                    .parse(requestToken.getAuthenticationURL())));

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }else{
                        String accessToken = sharedPref.getString(Constants.PREF_KEY_OAUTH_TOKEN,"");
                        String accessTokenSecret = sharedPref.getString(Constants.PREF_KEY_OAUTH_SECRET,"");

                        builder.setOAuthAccessToken(accessToken);
                        builder.setOAuthAccessTokenSecret(accessTokenSecret);

                        configuration = builder.build();
                        TwitterFactory factory = new TwitterFactory(configuration);
                        twitter = factory.getInstance();

                        Intent intent = new Intent(MainActivity.this,TwitterPop_Activity.class);
                        intent.putExtra(Constants.DATA_INTENT_TEMPERATURE, temperature);
                        intent.putExtra(Constants.DATA_INTENT_HUMIDITY, humidity);
                        intent.putExtra(Constants.DATA_INTENT_LOCATION, location);
                        intent.putExtra(Constants.DATA_INTENT_AIRQUALITY,airQuality);


                        startActivity(intent);        }
                }
            });
            thread.start();



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
        if (!sharedPref.getBoolean(Constants.KEEP_SIGNEDIN, false)){
            signOut();
        }
        super.onStop();
    }



}

